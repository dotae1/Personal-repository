package com.example.playlist.gemini.service;

import com.example.playlist.gemini.dto.GeminiRequest;
import com.example.playlist.gemini.dto.GeminiResponse;
import com.example.playlist.gemini.exception.GeminiErrorCode;
import com.example.playlist.gemini.exception.GeminiException;
import com.example.playlist.global.util.RedisUtil;
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
    private final RedisUtil redisUtil;

    private static final int RATE_LIMIT_PER_MINUTE = 5;  // IP당 분당 최대 요청 수

    public GeminiResponse CreatePlaylist(GeminiRequest request, String clientIp) throws JsonProcessingException {
        checkRateLimit(clientIp);

        try {
            GenerateContentResponse response =
                    geminiClient.models.generateContent(
                            "gemini-3-flash-preview",
                            request.toPrompt(),
                            geminiConfig
                    );

            String jsonText = response.text();
            return objectMapper.readValue(jsonText, GeminiResponse.class);

        } catch (com.google.genai.errors.ServerException e) {
            throw new GeminiException(GeminiErrorCode.GEMINI_IS_BUSY);
        }
    }

    private void checkRateLimit(String clientIp) {
        String key = "gemini:ratelimit:" + clientIp;
        String countStr = redisUtil.getData(key);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);

        if (count >= RATE_LIMIT_PER_MINUTE) {
            throw new GeminiException(GeminiErrorCode.GEMINI_RATE_LIMIT_EXCEEDED);
        }

        redisUtil.setDataExpire(key, String.valueOf(count + 1), 60); // 60초 TTL
    }
}
