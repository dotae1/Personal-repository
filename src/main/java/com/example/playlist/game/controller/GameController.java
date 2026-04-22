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
            description = "연대별 랜덤 트랙 반환. decade: 1990 / 2000 / 2010 / 2020"
    )
    @GetMapping("/game/quiz")
    public ResponseEntity<QuizTrackResponse> getQuizTrack(
            @RequestParam int decade
    ) {
        return ResponseEntity.ok(gameService.getQuizTrack(decade));
    }

    @Operation(
            summary = "[Admin] 퀴즈 트랙 수집",
            description = "연대별 한국 노래 100곡을 Spotify + Deezer에서 수집해 DB에 저장. decade: 1990 / 2000 / 2010 / 2020"
    )
    @PostMapping("/admin/game/collect")
    public ResponseEntity<CollectResponse> collectTracks(
            @RequestParam int decade
    ) {
        return ResponseEntity.ok(gameCollectService.collectTracks(decade));
    }
}
