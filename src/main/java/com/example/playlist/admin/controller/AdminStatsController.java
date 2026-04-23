package com.example.playlist.admin.controller;

import com.example.playlist.admin.dto.DailyStatsResponse;
import com.example.playlist.admin.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @Operation(
            summary = "[Admin] 일별 방문자 수 & AI 호출 횟수 조회",
            description = "오늘부터 days일 전까지의 방문자 수(유니크 IP)와 AI 추천 호출 횟수를 반환합니다."
    )
    @GetMapping("/visitors")
    public ResponseEntity<List<DailyStatsResponse>> getStats(
            @RequestParam(defaultValue = "7") int days
    ) {
        return ResponseEntity.ok(adminStatsService.getStats(days));
    }
}
