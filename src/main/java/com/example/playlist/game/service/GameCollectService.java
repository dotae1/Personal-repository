package com.example.playlist.game.service;

import com.example.playlist.game.dto.CollectResponse;
import com.example.playlist.game.dto.DeezerSearchResponse;
import com.example.playlist.game.entity.QuizTrack;
import com.example.playlist.game.repository.QuizTrackMapper;
import com.example.playlist.spotify.dto.SpotifySearchResponse;
import com.example.playlist.spotify.service.SpotifyTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameCollectService {

    private final SpotifyTokenService spotifyTokenService;
    private final WebClient spotifyWebClient;
    private final WebClient deezerWebClient;
    private final QuizTrackMapper quizTrackMapper;

    private static final int SEARCH_LIMIT = 10;
    private static final int TARGET = 100;
    private static final int MIN_POPULARITY = 0;
    private static final int MAX_OFFSET = 500; // 최대 500곡 탐색

    public CollectResponse collectTracks(int decade) {
        String yearRange = toYearRange(decade);
        String accessToken = spotifyTokenService.getAccessToken();

        int newlyCollected = 0;
        int offset = 0;

        while (newlyCollected < TARGET && offset < MAX_OFFSET) {
            List<SpotifySearchResponse.Item> items = searchSpotify(yearRange, accessToken, offset);

            if (items.isEmpty()) {
                log.info("[Collect] Spotify 결과 없음, offset={}", offset);
                break;
            }

            for (SpotifySearchResponse.Item item : items) {
                if (newlyCollected >= TARGET) break;
                if (item.getPopularity() < MIN_POPULARITY) continue;

                // 이미 DB에 있으면 스킵
                if (quizTrackMapper.existsBySpotifyTrackId(item.getTrackId())) {
                    log.debug("[Collect] 중복 스킵 - title={}", item.getName());
                    continue;
                }

                String artist = item.getArtists() != null && !item.getArtists().isEmpty()
                        ? item.getArtists().get(0).getName() : "";

                // Deezer preview 확인
                String previewUrl = getDeezerPreview(item.getName(), artist);
                if (previewUrl == null) {
                    log.debug("[Collect] Deezer preview 없음 - title={}", item.getName());
                    continue;
                }

                String albumImageUrl = null;
                if (item.getAlbum() != null && item.getAlbum().getImages() != null
                        && !item.getAlbum().getImages().isEmpty()) {
                    albumImageUrl = item.getAlbum().getImages().get(0).getUrl();
                }

                quizTrackMapper.insert(QuizTrack.builder()
                        .title(item.getName())
                        .artist(artist)
                        .albumImageUrl(albumImageUrl)
                        .previewUrl(previewUrl)
                        .decade(decade)
                        .spotifyTrackId(item.getTrackId())
                        .build());

                newlyCollected++;
                log.info("[Collect] 저장 - [{}/{}] title={}, artist={}, popularity={}",
                        newlyCollected, TARGET, item.getName(), artist, item.getPopularity());
            }

            offset += SEARCH_LIMIT;

            // Spotify rate limit 방지 딜레이
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        }

        int total = quizTrackMapper.countByDecade(decade);
        log.info("[Collect] 완료 - decade={}, 신규={}, DB총={}", decade, newlyCollected, total);
        return new CollectResponse(decade, newlyCollected, total);
    }

    private List<SpotifySearchResponse.Item> searchSpotify(String yearRange, String accessToken, int offset) {
        try {
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
            // popularity 필터 없이 원본 그대로 반환 (필터는 루프에서 처리)
            List<SpotifySearchResponse.Item> items = response.getTracks().getItems();
            log.info("[Collect] Spotify 검색 - offset={}, 결과={}곡", offset, items.size());
            return items;
        } catch (Exception e) {
            log.warn("[Collect] Spotify 검색 실패 - offset={}, error={}", offset, e.getMessage());
            return List.of();
        }
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
            log.warn("[Collect] Deezer 조회 실패 - title={}, error={}", title, e.getMessage());
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
