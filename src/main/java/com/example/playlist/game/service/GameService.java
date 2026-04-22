package com.example.playlist.game.service;

import com.example.playlist.game.dto.QuizTrackResponse;
import com.example.playlist.spotify.dto.SpotifySearchResponse;
import com.example.playlist.spotify.service.SpotifyTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final SpotifyTokenService spotifyTokenService;
    private final WebClient spotifyWebClient;
    private final Random random = new Random();

    private static final int SEARCH_LIMIT = 50;

    /**
     * 연대별 랜덤 퀴즈 트랙 반환 (preview_url 있는 것만)
     * decade: 1990 / 2000 / 2010 / 2020
     */
    public QuizTrackResponse getQuizTrack(int decade) {
        String yearRange = toYearRange(decade);
        String accessToken = spotifyTokenService.getAccessToken();

        // 랜덤 offset으로 다양한 곡 제공 (최대 200 범위)
        int offset = random.nextInt(200);

        List<SpotifySearchResponse.Item> candidates = searchWithPreview(yearRange, accessToken, offset);

        // 결과가 없으면 offset=0으로 재시도
        if (candidates.isEmpty()) {
            log.info("[Game] preview_url 없음, offset=0 재시도 - yearRange={}", yearRange);
            candidates = searchWithPreview(yearRange, accessToken, 0);
        }

        if (candidates.isEmpty()) {
            throw new IllegalStateException("해당 연대의 미리듣기 가능한 트랙을 찾을 수 없습니다.");
        }

        SpotifySearchResponse.Item picked = candidates.get(random.nextInt(candidates.size()));
        log.info("[Game] 퀴즈 트랙 선택 - decade={}, title={}", decade, picked.getName());

        return QuizTrackResponse.from(picked);
    }

    private List<SpotifySearchResponse.Item> searchWithPreview(String yearRange, String accessToken, int offset) {
        SpotifySearchResponse response = spotifyWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", "year:" + yearRange)
                        .queryParam("type", "track")
                        .queryParam("limit", SEARCH_LIMIT)
                        .queryParam("offset", offset)
                        .queryParam("market", "KR")
                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(SpotifySearchResponse.class)
                .block();

        if (response == null || response.getTracks() == null || response.getTracks().getItems() == null) {
            return List.of();
        }

        return response.getTracks().getItems().stream()
                .filter(item -> item.getPreviewUrl() != null && !item.getPreviewUrl().isBlank())
                .toList();
    }

    private String toYearRange(int decade) {
        return switch (decade) {
            case 1990 -> "1990-1999";
            case 2000 -> "2000-2009";
            case 2010 -> "2010-2019";
            case 2020 -> "2020-2029";
            default -> throw new IllegalArgumentException("지원하지 않는 연대입니다: " + decade);
        };
    }
}
