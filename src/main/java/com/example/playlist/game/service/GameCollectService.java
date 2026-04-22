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
import java.util.Map;

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

    private static final Map<Integer, List<String>> KOREAN_ARTISTS = Map.of(
        1990, List.of(
            "서태지와 아이들", "김건모", "신승훈", "H.O.T.", "S.E.S.",
            "god", "Fin.K.L", "젝스키스", "이승환", "조성모", "클론",
            "박진영", "유승준", "룰라", "015B", "김광석", "이적",
            "노이즈", "솔리드", "듀스", "DJ DOC", "토이", "패닉"
        ),
        2000, List.of(
            "TVXQ", "Big Bang", "Wonder Girls", "Girls Generation", "2NE1",
            "Super Junior", "KARA", "2PM", "SHINee", "BoA", "SS501",
            "이효리", "비", "버즈", "브라운아이드걸스", "에픽하이",
            "원더걸스", "f(x)", "씨스타", "4Minute", "지드래곤", "태양"
        ),
        2010, List.of(
            "BTS", "EXO", "TWICE", "BLACKPINK", "Red Velvet",
            "IU", "PSY", "INFINITE", "SISTAR", "GOT7", "Wanna One",
            "NCT 127", "MAMAMOO", "AOA", "B2ST", "2AM", "케이윌",
            "악동뮤지션", "볼빨간사춘기", "선미", "CL", "지코"
        ),
        2020, List.of(
            "BTS", "BLACKPINK", "aespa", "IVE", "NewJeans",
            "Stray Kids", "ITZY", "SEVENTEEN", "LE SSERAFIM", "NCT 127",
            "ENHYPEN", "G I-DLE", "TXT", "ATEEZ", "Kep1er",
            "NMIXX", "fromis 9", "BTOB", "임영웅", "이찬원"
        )
    );

    public CollectResponse collectTracks(int decade) {
        String yearRange = toYearRange(decade);
        String accessToken = spotifyTokenService.getAccessToken();

        List<String> artists = KOREAN_ARTISTS.get(decade);
        int newlyCollected = 0;

        for (String artist : artists) {
            if (newlyCollected >= TARGET) break;

            List<SpotifySearchResponse.Item> items = searchSpotifyByArtist(artist, yearRange, accessToken);
            log.info("[Collect] 아티스트={}, 결과={}곡", artist, items.size());

            for (SpotifySearchResponse.Item item : items) {
                if (newlyCollected >= TARGET) break;

                // 이미 DB에 있으면 스킵
                if (quizTrackMapper.existsBySpotifyTrackId(item.getTrackId())) {
                    log.debug("[Collect] 중복 스킵 - title={}", item.getName());
                    continue;
                }

                String trackArtist = item.getArtists() != null && !item.getArtists().isEmpty()
                        ? item.getArtists().get(0).getName() : "";

                // Deezer preview 확인
                String previewUrl = getDeezerPreview(item.getName(), trackArtist);
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
                        .artist(trackArtist)
                        .albumImageUrl(albumImageUrl)
                        .previewUrl(previewUrl)
                        .decade(decade)
                        .spotifyTrackId(item.getTrackId())
                        .build());

                newlyCollected++;
                log.info("[Collect] 저장 - [{}/{}] title={}, artist={}, popularity={}",
                        newlyCollected, TARGET, item.getName(), trackArtist, item.getPopularity());
            }

            // Spotify rate limit 방지 딜레이
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }

        int total = quizTrackMapper.countByDecade(decade);
        log.info("[Collect] 완료 - decade={}, 신규={}, DB총={}", decade, newlyCollected, total);
        return new CollectResponse(decade, newlyCollected, total);
    }

    private List<SpotifySearchResponse.Item> searchSpotifyByArtist(String artist, String yearRange, String accessToken) {
        try {
            SpotifySearchResponse response = spotifyWebClient
                    .get()
                    .uri("/search?q={q}&type=track&limit={limit}&market=KR",
                            "artist:" + artist + " year:" + yearRange, SEARCH_LIMIT)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(SpotifySearchResponse.class)
                    .block();

            if (response == null || response.getTracks() == null || response.getTracks().getItems() == null) {
                return List.of();
            }
            return response.getTracks().getItems();
        } catch (Exception e) {
            log.warn("[Collect] Spotify 검색 실패 - artist={}, error={}", artist, e.getMessage());
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
