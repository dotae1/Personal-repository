package com.example.playlist.playlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SaveNewPlaylistRequest {

    @NotBlank
    private String playlistName;

    @NotBlank
    private String prompt;

    @NotEmpty
    private List<SpotifyTrackSaveRequest> tracks;
}
