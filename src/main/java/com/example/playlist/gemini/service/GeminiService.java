package com.example.playlist.gemini.service;

import com.example.playlist.gemini.dto.GeminiRequest;
import com.example.playlist.gemini.dto.GeminiResponse;
import com.example.playlist.gemini.exception.GeminiErrorCode;
import com.example.playlist.gemini.exception.GeminiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final Client geminiClient;
    private final GenerateContentConfig geminiConfig;
    private final ObjectMapper objectMapper;

    public GeminiResponse CreatePlaylist(GeminiRequest request) throws JsonProcessingException {
        try {
            GenerateContentResponse response =
                    geminiClient.models.generateContent(
                            "gemini-3-flash-preview",
                            request.toPrompt(),
                            geminiConfig
                    );

            String jsonText = response.text();
            GeminiResponse geminiResponse = objectMapper.readValue(jsonText, GeminiResponse.class);

            return geminiResponse;
        } catch (com.google.genai.errors.ServerException e) {
            throw new GeminiException(GeminiErrorCode.GEMINI_IS_BUSY);
        }
    }
}
