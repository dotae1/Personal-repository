package com.example.playlist.game.controller;

import com.example.playlist.game.dto.QuizTrackResponse;
import com.example.playlist.game.service.GameCollectService;
import com.example.playlist.game.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GameCollectService gameCollectService;

    @Operation(
            summary = "퀴즈 트랙 조회",
            description = "연대별 랜덤 트랙 반환. decade: 1990_EARLY / 1990_LATE / 2000 / 2010 / 2020"
    )
    @GetMapping("/game/quiz")
    public ResponseEntity<QuizTrackResponse> getQuizTrack(
            @RequestParam String decade
    ) {
        return ResponseEntity.ok(gameService.getQuizTrack(decade));
    }

    @Operation(
            summary = "[Admin] 퀴즈 트랙 수집 (비동기)",
            description = "백그라운드에서 Gemini 추천 + iTunes preview 검증 후 DB 저장 (50곡, 중복 제외). decade: 1990_EARLY / 1990_LATE / 2000 / 2010 / 2020"
    )
    @PostMapping("/admin/game/collect")
    public ResponseEntity<Map<String, String>> collectTracks(
            @RequestParam String decade
    ) {
        gameCollectService.collectTracksAsync(decade);
        return ResponseEntity.accepted().body(Map.of(
                "message", "수집이 백그라운드에서 시작되었습니다. 서버 로그를 확인하세요.",
                "decade", decade
        ));
    }

    @Operation(
            summary = "[Admin] 연대별 퀴즈 트랙 수 조회",
            description = "decade: 1990_EARLY / 1990_LATE / 2000 / 2010 / 2020"
    )
    @GetMapping("/admin/game/count")
    public ResponseEntity<Map<String, Object>> countTracks(
            @RequestParam String decade
    ) {
        int count = gameCollectService.countByDecade(decade);
        return ResponseEntity.ok(Map.of("decade", decade, "count", count));
    }
}
