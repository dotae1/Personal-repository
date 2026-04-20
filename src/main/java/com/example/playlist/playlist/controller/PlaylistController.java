package com.example.playlist.playlist.controller;

import com.example.playlist.gemini.dto.GeminiRequest;
import com.example.playlist.global.success.SuccessResponse;
import com.example.playlist.playlist.dto.PlaylistResponse;
import com.example.playlist.playlist.exception.PlaylistSuccessCode;
import com.example.playlist.playlist.dto.SaveNewPlaylistRequest;
import com.example.playlist.playlist.dto.SaveToExistingPlaylistRequest;
import com.example.playlist.playlist.service.PlaylistService;
import com.example.playlist.spotify.dto.SpotifyUserPlaylistsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
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
            @RequestBody GeminiRequest geminiRequest
    ) throws JsonProcessingException {
        return playlistService.createPlayList(geminiRequest);
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
