package com.url_shortner.service;

import com.url_shortner.dto.AnalyticsResponse;
import com.url_shortner.entity.Click;
import com.url_shortner.repository.ClickRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ClickRepository clickRepository;
    private final Parser uaParser =  new Parser();

    @Async
    public void logClick(String shortCode, String ipAddress, String userAgent) {
        Client client = uaParser.parse(userAgent);

        Click click = Click.builder()
                .shortCode(shortCode)
                .clickedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .browser(client.userAgent.family)
                .os(client.os.family)
                .device(client.device.family)
                .build();

        clickRepository.save(click);
    }
    public AnalyticsResponse getAnalytics(String shortCode) {
        long totalClicks = clickRepository.countByShortCode(shortCode);

        List<Click> clicks = clickRepository.findByShortCode(shortCode);

        Map<String, Long> browserBreakdown = clicks.stream()
                .collect(Collectors.groupingBy(Click::getBrowser, Collectors.counting()));

        Map<String, Long> deviceBreakdown = clicks.stream()
                .collect(Collectors.groupingBy(Click::getDevice, Collectors.counting()));

        Map<String, Long> clicksPerDay = clicks.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getClickedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        return AnalyticsResponse.builder()
                .totalClicks(totalClicks)
                .browserBreakdown(browserBreakdown)
                .deviceBreakdown(deviceBreakdown)
                .clicksPerDay(clicksPerDay)
                .build();
    }

}
