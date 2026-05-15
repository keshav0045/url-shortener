package com.url_shortner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    private long totalClicks;
    private Map<String, Long> clicksPerDay;
    private Map<String, Long> browserBreakdown;
    private Map<String, Long> deviceBreakdown;
}
