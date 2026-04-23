package com.example.playlist.gemini.controller;

import com.example.playlist.gemini.dto.GeminiResponse;
import com.example.playlist.gemini.service.GeminiService;
import com.example.playlist.gemini.dto.GeminiRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
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

    @Operation(
            summary = "AI 플레이리스트 생성",
            description = "사용자가 입력한 프롬프트를 통해 원하는 플레이리스트를 추천해주는 컨트롤러"
    )
    @PostMapping("/playlist")
    public GeminiResponse createPlaylist(
            @RequestBody GeminiRequest request,
            HttpServletRequest httpRequest
    ) throws JsonProcessingException {
        String clientIp = getClientIp(httpRequest);
        return geminiService.CreatePlaylist(request, clientIp);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim(); // 프록시 체인의 첫 번째 IP
        }
        return request.getRemoteAddr();
    }
}
