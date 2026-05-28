package com.url_shortner.service;

import com.url_shortner.dto.ShortenRequest;
import com.url_shortner.dto.ShortenResponse;
import com.url_shortner.entity.Url;
import com.url_shortner.repository.UrlRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {
    private final Base62Service base62Service;
    private final UrlRepository urlRepository;
    private final AnalyticsService analyticsService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public ShortenResponse shorten(ShortenRequest request) {
        Url url = Url.builder()
                .longUrl(request.getLongUrl())
                .expiresAt(LocalDateTime.now().plusDays(request.getExpiryDays()))
                .build();

        Url saved = urlRepository.save(url);

        String shortCode = base62Service.encode(saved.getId());
        saved.setShortCode(shortCode);
        urlRepository.save(saved);

        return ShortenResponse.builder()
                .shortUrl(baseUrl + "/" + shortCode)
                .shortCode(shortCode)
                .expiresAt(saved.getExpiresAt())
                .build();
    }

    public String getOriginalUrl(String shortCode, HttpServletRequest request) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found"));

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Short URL has expired");
        }


        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        analyticsService.logClick(shortCode, ipAddress, userAgent);

        return url.getLongUrl();
    }
}