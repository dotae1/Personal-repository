package com.example.playlist.game.controller;

import com.example.playlist.game.dto.QuizTrackResponse;
import com.example.playlist.game.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @Operation(
            summary = "퀴즈 트랙 조회",
            description = "연대별 랜덤 트랙 반환 (미리듣기 URL 포함). decade: 1990 / 2000 / 2010 / 2020"
    )
    @GetMapping("/quiz")
    public ResponseEntity<QuizTrackResponse> getQuizTrack(
            @RequestParam int decade
    ) {
        return ResponseEntity.ok(gameService.getQuizTrack(decade));
    }
}
