package com.example.playlist.playlist.controller;

import com.example.playlist.gemini.dto.GeminiRequest;
import com.example.playlist.global.success.SuccessResponse;
import com.example.playlist.playlist.dto.PlaylistDetailResponse;
import com.example.playlist.playlist.dto.PlaylistResponse;
import com.example.playlist.playlist.exception.PlaylistSuccessCode;
import com.example.playlist.playlist.dto.SaveNewPlaylistRequest;
import com.example.playlist.playlist.dto.SaveToExistingPlaylistRequest;
import com.example.playlist.playlist.service.PlaylistService;
import com.example.playlist.spotify.dto.SpotifyUserPlaylistsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/playlist")
public class PlaylistController {

    private final PlaylistService playlistService;

    @Operation(summary = "AI 플레이리스트 추천", description = "Gemini가 추천한 곡들을 Spotify에서 검색해서 반환 (DB 미저장)")
    @PostMapping
    public PlaylistResponse createPlaylist(
            @RequestBody GeminiRequest geminiRequest,
            HttpServletRequest httpRequest
    ) throws JsonProcessingException {
        String clientIp = getClientIp(httpRequest);
        return playlistService.createPlayList(geminiRequest, clientIp);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Operation(summary = "플레이리스트 상세 조회", description = "플레이리스트 정보 + 곡 목록 반환 (본인 것만)")
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<PlaylistDetailResponse>> getPlaylistDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PlaylistDetailResponse result = playlistService.getPlaylistDetail(userDetails.getUsername(), id);
        return ResponseEntity.ok(SuccessResponse.of(PlaylistSuccessCode.PLAYLIST_DETAIL, result));
    }

    @Operation(summary = "플레이리스트 삭제", description = "본인 플레이리스트 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> deletePlaylist(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        playlistService.deletePlaylist(userDetails.getUsername(), id);
        return ResponseEntity.ok(SuccessResponse.of(PlaylistSuccessCode.PLAYLIST_DELETED));
    }

    @Operation(summary = "내 Spotify 플레이리스트 목록 조회", description = "기존 플레이리스트에 추가하기 전 목록 불러오기")
    @GetMapping("/spotify/my-playlists")
    public ResponseEntity<SuccessResponse<SpotifyUserPlaylistsResponse>> getMySpotifyPlaylists(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SpotifyUserPlaylistsResponse result = playlistService.getUserSpotifyPlaylists(userDetails.getUsername());
        return ResponseEntity.ok(SuccessResponse.of(PlaylistSuccessCode.PLAYLIST_ADDED, result));
    }

    @Operation(summary = "새 Spotify 플레이리스트 생성 후 저장", description = "새 플레이리스트를 만들고 추천곡 추가 + DB 저장")
    @PostMapping("/spotify/save-new")
    public ResponseEntity<SuccessResponse> saveAsNewPlaylist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid SaveNewPlaylistRequest request
    ) throws InterruptedException {
        playlistService.saveAsNewPlaylist(userDetails.getUsername(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.of(PlaylistSuccessCode.PLAYLIST_SAVED));
    }

    @Operation(summary = "기존 Spotify 플레이리스트에 추가 + 저장", description = "선택한 기존 플레이리스트에 추천곡 추가 + DB 저장")
    @PostMapping("/spotify/save-existing")
    public ResponseEntity<SuccessResponse> saveToExistingPlaylist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid SaveToExistingPlaylistRequest request
    ) {
        playlistService.saveToExistingPlaylist(userDetails.getUsername(), request);
        return ResponseEntity.ok(SuccessResponse.of(PlaylistSuccessCode.PLAYLIST_ADDED));
    }
}
