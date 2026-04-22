package com.example.playlist.game.service;

import com.example.playlist.game.dto.DeezerSearchResponse;
import com.example.playlist.game.dto.QuizTrackResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final Client geminiClient;
    private final GenerateContentConfig geminiConfig;
    private final WebClient deezerWebClient;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRY = 3;

    public QuizTrackResponse getQuizTrack(int decade) {
        String decadeLabel = toDecadeLabel(decade);

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                // 1. Gemini에서 한국 노래 추천
                SongInfo song = recommendFromGemini(decadeLabel, attempt);
                log.info("[Game] Gemini 추천 - title={}, artist={}", song.title, song.artist);

                // 2. Deezer에서 preview URL 조회 (영어 검색어 사용)
                DeezerSearchResponse deezerRes = deezerWebClient
                        .get()
                        .uri("/search?q={q}&limit=1", song.searchQuery)
                        .retrieve()
                        .bodyToMono(DeezerSearchResponse.class)
                        .block();

                if (deezerRes == null || deezerRes.getData() == null || deezerRes.getData().isEmpty()) {
                    log.warn("[Game] Deezer 결과 없음 - attempt={}, title={}", attempt, song.title);
                    continue;
                }

                DeezerSearchResponse.DeezerTrack track = deezerRes.getData().get(0);
                String previewUrl = track.getPreview();

                if (previewUrl == null || previewUrl.isBlank()) {
                    log.warn("[Game] Deezer preview 없음 - attempt={}, title={}", attempt, song.title);
                    continue;
                }

                log.info("[Game] 최종 선택 - title={}, artist={}", song.title, song.artist);
                return QuizTrackResponse.fromGemini(song.title, song.artist, previewUrl, track.getAlbumImageUrl());

            } catch (Exception e) {
                log.warn("[Game] 시도 실패 - attempt={}, error={}", attempt, e.getMessage());
            }
        }

        throw new IllegalStateException("퀴즈 트랙을 가져오는 데 실패했습니다. 잠시 후 다시 시도해주세요.");
    }

    private SongInfo recommendFromGemini(String decadeLabel, int attempt) throws Exception {
        String prompt = String.format("""
                %s 한국 가요/K-pop 노래 중 랜덤으로 1곡만 추천해줘.
                조건:
                - 반드시 한국 가수의 한국 노래
                - title과 artist는 한국어로 (예: 방탄소년단, 아이유)
                - searchQuery는 Deezer 검색용 영어 표기 (예: "BTS Dynamite", "IU Celebrity")
                - 실제 스트리밍 서비스에 존재하는 곡
                - 매번 다른 곡 추천
                - 아래 JSON 형식으로만 응답 (다른 텍스트 없이):
                {"title": "곡제목(한국어)", "artist": "아티스트명(한국어)", "searchQuery": "영어 검색어"}
                """, decadeLabel);

        GenerateContentResponse response = geminiClient.models.generateContent(
                "gemini-3-flash-preview",
                prompt,
                geminiConfig
        );

        String json = response.text().trim()
                .replaceAll("```json", "").replaceAll("```", "").trim();

        log.info("[Game] Gemini raw json={}", json);

        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);

        // Gemini가 {"songs": [...]} 형태로 반환하는 경우 처리
        if (node.has("songs") && node.get("songs").isArray() && !node.get("songs").isEmpty()) {
            node = node.get("songs").get(0);
        }

        String title = node.get("title").asText();
        String artist = node.get("artist").asText();
        String searchQuery = node.has("searchQuery") ? node.get("searchQuery").asText() : artist + " " + title;
        return new SongInfo(title, artist, searchQuery);
    }

    private String toDecadeLabel(int decade) {
        return switch (decade) {
            case 1990 -> "1990년대(1990~1999년)";
            case 2000 -> "2000년대(2000~2009년)";
            case 2010 -> "2010년대(2010~2019년)";
            case 2020 -> "2020년대(2020~현재)";
            default -> throw new IllegalArgumentException("지원하지 않는 연대입니다: " + decade);
        };
    }

    record SongInfo(String title, String artist, String searchQuery) {}
}
