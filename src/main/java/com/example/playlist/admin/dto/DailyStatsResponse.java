package com.example.playlist.admin.dto;

import java.util.Set;

public record DailyStatsResponse(
        String date,
        long visitorCount,
        long aiCallCount,
        Set<String> memberVisitors
) {}
