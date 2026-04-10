package com.example.playlist.Entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Song {
    private Long id;
    private String spotifyTrackId;
    private String spotifyTrackUri;
    private String title;
    private String artist;
    private String album;
}