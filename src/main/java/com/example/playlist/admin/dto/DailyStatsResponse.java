package com.example.playlist.admin.dto;

public record DailyStatsResponse(
        String date,
        long visitorCount,
        long aiCallCount
) {}
