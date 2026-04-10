package com.example.playlist.gemini.controller;

import com.example.playlist.gemini.dto.GeminiResponse;
import com.example.playlist.gemini.service.GeminiService;
import com.example.playlist.gemini.dto.GeminiRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gemini")
public class GeminiController {

    private final GeminiService geminiService;

    @PostMapping("/playlist")
    public GeminiResponse createPlaylist(
            @RequestBody GeminiRequest reqeust
            ) throws JsonProcessingException {
        GeminiResponse response = geminiService.CreatePlaylist(reqeust);
        return response;
    }
}
