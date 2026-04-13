package com.example.playlist.playlist.controller;

import com.example.playlist.gemini.dto.GeminiRequest;
import com.example.playlist.playlist.dto.PlaylistResponse;
import com.example.playlist.playlist.service.PlaylistService;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    @PostMapping
    public PlaylistResponse createPlaylist(
            @RequestBody GeminiRequest geminiRequest
            ) throws JsonProcessingException {
        return playlistService.createPlayList(geminiRequest);
    }

}
