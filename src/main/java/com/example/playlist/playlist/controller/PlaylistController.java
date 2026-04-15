package com.example.playlist.playlist.controller;

import com.example.playlist.gemini.dto.GeminiRequest;
import com.example.playlist.playlist.dto.PlaylistResponse;
import com.example.playlist.playlist.service.PlaylistService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlist")
public class PlaylistController {

    private final PlaylistService playlistService;

    @Operation(
            summary = "실제 플레이리스트 반환",
            description = "GEMINI가 추천해준 플레이리스트를 Spotify API를 활용해 검색해주는 컨트롤러"
    )
    @PostMapping
    public PlaylistResponse createPlaylist(
            @RequestBody GeminiRequest geminiRequest
            ) throws JsonProcessingException {
        return playlistService.createPlayList(geminiRequest);
    }

}
