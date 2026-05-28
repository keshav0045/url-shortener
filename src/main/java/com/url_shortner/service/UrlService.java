package com.url_shortner.service;

import com.url_shortner.dto.ShortenRequest;
import com.url_shortner.dto.ShortenResponse;
import com.url_shortner.entity.Url;
import com.url_shortner.repository.UrlRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final Base62Service base62Service;
    private final AnalyticsService analyticsService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.cache.ttl}")
    private long cacheTtl;

    @Transactional
    public ShortenResponse shorten(ShortenRequest request) {
        Url url = Url.builder()
                .longUrl(request.getLongUrl())
                .expiresAt(LocalDateTime.now().plusDays(request.getExpiryDays()))
                .build();

        Url saved = urlRepository.save(url);
        urlRepository.flush();

        String shortCode = base62Service.encode(saved.getId());
        saved.setShortCode(shortCode);
        urlRepository.save(saved);

        // cache it immediately after shortening
        redisTemplate.opsForValue().set(
                "url:" + shortCode,
                request.getLongUrl(),
                cacheTtl,
                TimeUnit.SECONDS
        );

        return ShortenResponse.builder()
                .shortUrl(baseUrl + "/" + shortCode)
                .shortCode(shortCode)
                .expiresAt(saved.getExpiresAt())
                .build();
    }

    public String getOriginalUrl(String shortCode, HttpServletRequest request) {

        // check Redis first
        String cachedUrl = redisTemplate.opsForValue().get("url:" + shortCode);

        if (cachedUrl != null) {
            System.out.println("cache hit for: " + shortCode);
            analyticsService.logClick(shortCode,
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"));
            return cachedUrl;
        }

        // cache miss — go to DB
        System.out.println("cache miss for: " + shortCode);
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found"));

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Short URL has expired");
        }

        // save to Redis for next time
        redisTemplate.opsForValue().set(
                "url:" + shortCode,
                url.getLongUrl(),
                cacheTtl,
                TimeUnit.SECONDS
        );

        analyticsService.logClick(shortCode,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        return url.getLongUrl();
    }
}