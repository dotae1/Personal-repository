package com.example.playlist.game.service;

import com.example.playlist.game.dto.DeezerSearchResponse;
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
    private final WebClient deezerWebClient;
    private final Random random = new Random();

    private static final int SEARCH_LIMIT = 10;

    /**
     * 연대별 랜덤 퀴즈 트랙 반환
     * decade: 1990 / 2000 / 2010 / 2020
     */
    public QuizTrackResponse getQuizTrack(int decade) {
        String yearRange = toYearRange(decade);
        String accessToken = spotifyTokenService.getAccessToken();

        int offset = random.nextInt(990);
        List<SpotifySearchResponse.Item> candidates = searchSpotify(yearRange, accessToken, offset);

        if (candidates.isEmpty()) {
            candidates = searchSpotify(yearRange, accessToken, 0);
        }

        if (candidates.isEmpty()) {
            throw new IllegalStateException("해당 연대의 트랙을 찾을 수 없습니다.");
        }

        // Deezer preview가 있는 트랙을 찾을 때까지 순회
        for (int i = 0; i < candidates.size(); i++) {
            SpotifySearchResponse.Item picked = candidates.get(random.nextInt(candidates.size()));
            String artist = picked.getArtists() != null && !picked.getArtists().isEmpty()
                    ? picked.getArtists().get(0).getName() : "";

            String previewUrl = getDeezerPreview(picked.getName(), artist);
            if (previewUrl != null) {
                log.info("[Game] 퀴즈 트랙 선택 - decade={}, title={}", decade, picked.getName());
                return QuizTrackResponse.from(picked, previewUrl);
            }
        }

        // 모두 실패 시 첫 번째 트랙을 previewUrl=null로 반환
        SpotifySearchResponse.Item fallback = candidates.get(0);
        log.warn("[Game] Deezer preview 없음, previewUrl=null 반환 - title={}", fallback.getName());
        return QuizTrackResponse.from(fallback, null);
    }

    private List<SpotifySearchResponse.Item> searchSpotify(String yearRange, String accessToken, int offset) {
        SpotifySearchResponse response = spotifyWebClient
                .get()
                .uri("/search?q={q}&type=track&limit={limit}&offset={offset}&market=KR",
                        "year:" + yearRange + " genre:k-pop", SEARCH_LIMIT, offset)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(SpotifySearchResponse.class)
                .block();

        if (response == null || response.getTracks() == null || response.getTracks().getItems() == null) {
            return List.of();
        }

        return response.getTracks().getItems();
    }

    private String getDeezerPreview(String title, String artist) {
        try {
            DeezerSearchResponse response = deezerWebClient
                    .get()
                    .uri("/search?q={q}&limit=1", artist + " " + title)
                    .retrieve()
                    .bodyToMono(DeezerSearchResponse.class)
                    .block();

            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                return null;
            }

            String preview = response.getData().get(0).getPreview();
            return (preview != null && !preview.isBlank()) ? preview : null;
        } catch (Exception e) {
            log.warn("[Game] Deezer 조회 실패 - title={}, error={}", title, e.getMessage());
            return null;
        }
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
