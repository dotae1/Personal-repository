package com.example.playlist.game.service;

import com.example.playlist.game.dto.DeezerSearchResponse;
import com.example.playlist.game.dto.QuizTrackResponse;
import com.example.playlist.spotify.dto.SpotifySearchResponse;
import com.example.playlist.spotify.service.SpotifyTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final SpotifyTokenService spotifyTokenService;
    private final WebClient spotifyWebClient;
    private final WebClient deezerWebClient;
    private final Random random = new Random();

    private static final int SEARCH_LIMIT = 10;
    private static final int MIN_POPULARITY = 40;
    private static final int MAX_OFFSET = 150;

    public QuizTrackResponse getQuizTrack(int decade) {
        String yearRange = toYearRange(decade);
        String accessToken = spotifyTokenService.getAccessToken();

        // 서로 다른 offset으로 2회 검색 → 최대 20곡 풀 구성
        Set<String> seenIds = new HashSet<>();
        List<SpotifySearchResponse.Item> pool = new ArrayList<>();

        int offset1 = random.nextInt(MAX_OFFSET);
        int offset2 = random.nextInt(MAX_OFFSET);

        for (int offset : new int[]{offset1, offset2}) {
            searchSpotify(yearRange, accessToken, offset).stream()
                    .filter(item -> item.getPopularity() >= MIN_POPULARITY)
                    .filter(item -> seenIds.add(item.getTrackId()))
                    .forEach(pool::add);
        }

        // popularity 기준 미달 시 offset=0으로 제한 없이 재시도
        if (pool.isEmpty()) {
            log.info("[Game] popularity 기준 미달, offset=0 재시도 - yearRange={}", yearRange);
            pool = searchSpotify(yearRange, accessToken, 0);
        }

        if (pool.isEmpty()) {
            throw new IllegalStateException("해당 연대의 트랙을 찾을 수 없습니다.");
        }

        // popularity 내림차순 정렬 후 상위 절반에서 랜덤 선택 (인기곡 위주 + 다양성 확보)
        pool.sort((a, b) -> b.getPopularity() - a.getPopularity());
        int pickRange = Math.max(1, pool.size() / 2);
        SpotifySearchResponse.Item picked = pool.get(random.nextInt(pickRange));

        String artist = picked.getArtists() != null && !picked.getArtists().isEmpty()
                ? picked.getArtists().get(0).getName() : "";

        String previewUrl = getDeezerPreview(picked.getName(), artist);
        log.info("[Game] 퀴즈 트랙 선택 - decade={}, title={}, artist={}, popularity={}",
                decade, picked.getName(), artist, picked.getPopularity());

        return QuizTrackResponse.from(picked, previewUrl);
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
