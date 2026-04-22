package com.example.playlist.game.controller;

import com.example.playlist.game.dto.CollectResponse;
import com.example.playlist.game.dto.QuizTrackResponse;
import com.example.playlist.game.service.GameCollectService;
import com.example.playlist.game.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            summary = "[Admin] 퀴즈 트랙 수집",
            description = "Gemini 추천 + iTunes preview 검증 후 DB 저장 (50곡, 중복 제외). decade: 1990_EARLY / 1990_LATE / 2000 / 2010 / 2020"
    )
    @PostMapping("/admin/game/collect")
    public ResponseEntity<CollectResponse> collectTracks(
            @RequestParam String decade
    ) {
        return ResponseEntity.ok(gameCollectService.collectTracks(decade));
    }
}
