package com.example.playlist.playlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SaveToExistingPlaylistRequest {

    /** Spotify 플레이리스트 ID (사용자가 선택한 기존 플레이리스트) */
    @NotBlank
    private String targetSpotifyPlaylistId;

    @NotBlank
    private String playlistName;

    @NotBlank
    private String prompt;

    @NotEmpty
    private List<SpotifyTrackSaveRequest> tracks;
}
