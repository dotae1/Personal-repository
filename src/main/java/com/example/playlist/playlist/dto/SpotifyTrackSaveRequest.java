package com.example.playlist.playlist.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpotifyTrackSaveRequest {
    private String spotifyTrackId;
    private String spotifyTrackUri;
    private String title;
    private String artist;
    private String album;
}
