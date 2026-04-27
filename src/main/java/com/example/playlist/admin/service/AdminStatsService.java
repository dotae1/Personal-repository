package com.example.playlist.admin.service;

import com.example.playlist.admin.dto.DailyStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final StringRedisTemplate redisTemplate;

    public List<DailyStatsResponse> getStats(int days) {
        List<DailyStatsResponse> result = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().minusDays(i);

            Long visitorCount = redisTemplate.opsForSet().size("visitor:" + date);
            String aiCallStr = redisTemplate.opsForValue().get("ai:calls:" + date);
            Set<String> memberVisitors = redisTemplate.opsForSet().members("visitor:member:" + date);

            result.add(new DailyStatsResponse(
                    date.toString(),
                    visitorCount != null ? visitorCount : 0L,
                    aiCallStr != null ? Long.parseLong(aiCallStr) : 0L,
                    memberVisitors != null ? memberVisitors : Set.of()
            ));
        }

        return result;
    }
}
