package com.example.playlist.spotify.service;

import com.example.playlist.spotify.dto.SpotifyCreatePlaylistResponse;
import com.example.playlist.spotify.dto.SpotifyCurrentUserResponse;
import com.example.playlist.spotify.dto.SpotifySearchResponse;
import com.example.playlist.spotify.dto.SpotifyTrack;
import com.example.playlist.spotify.dto.SpotifyUserPlaylistsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final SpotifyTokenService spotifyTokenService;
    private final WebClient spotifyWebClient;

    // ─────────────────────────────────────────────
    // 클라이언트 자격증명 토큰 사용 (트랙 검색)
    // ─────────────────────────────────────────────

    public Mono<SpotifyTrack> searchTrack(String artist, String title) {
        String accessToken = spotifyTokenService.getAccessToken();

        return spotifyWebClient
                .get()
                .uri("/search?q={q}&type=track&limit=1&market=KR", artist + " " + title)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(SpotifySearchResponse.class)
                .map(response -> SpotifyTrack.from(response.getTracks().getItems().get(0), title, artist));
    }

    // ─────────────────────────────────────────────
    // 유저 액세스 토큰 사용 (플레이리스트 관련)
    // ─────────────────────────────────────────────

    /** 현재 Spotify 유저 프로필 조회 */
    public SpotifyCurrentUserResponse getCurrentUser(String userToken) {
        return spotifyWebClient
                .get()
                .uri("/me")
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .bodyToMono(SpotifyCurrentUserResponse.class)
                .block();
    }

    /** 유저가 소유한 Spotify 플레이리스트 목록 조회 (팔로우 플레이리스트 제외) */
    public SpotifyUserPlaylistsResponse getUserPlaylists(String userToken) {
        String mySpotifyId = getCurrentUser(userToken).getId();

        SpotifyUserPlaylistsResponse all = spotifyWebClient
                .get()
                .uri("/me/playlists?limit=50")
                .header("Authorization", "Bearer " + userToken)
                .retrieve()
                .bodyToMono(SpotifyUserPlaylistsResponse.class)
                .block();

        if (all != null && all.getItems() != null) {
            all.getItems().removeIf(item ->
                    item.getOwner() == null || !mySpotifyId.equals(item.getOwner().getId())
            );
        }
        return all;
    }

    /** 새 Spotify 플레이리스트 생성 후 ID 반환 */
    public String createPlaylist(String name, String userToken) {
        SpotifyCreatePlaylistResponse response = spotifyWebClient
                .post()
                .uri("/me/playlists")
                .header("Authorization", "Bearer " + userToken)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "name", name,
                        "public", false,
                        "description", "AI 추천 플레이리스트"
                ))
                .retrieve()
                .bodyToMono(SpotifyCreatePlaylistResponse.class)
                .block();

        return response.getId();
    }

    /**
     * Spotify 플레이리스트에 트랙 추가
     * 2025년 2월부터 /tracks → /items 엔드포인트로 변경
     */
    public void addTracksToPlaylist(String playlistId, List<String> trackUris, String userToken) {
        spotifyWebClient
                .post()
                .uri("/playlists/{playlistId}/items", playlistId)
                .header("Authorization", "Bearer " + userToken)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of("uris", trackUris))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("[Spotify] 트랙 추가 실패 - status={}, body={}", response.statusCode(), errorBody);
                            return Mono.error(new RuntimeException("Spotify 트랙 추가 실패: " + errorBody));
                        })
                )
                .bodyToMono(Void.class)
                .block();
    }
}
