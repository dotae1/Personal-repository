package com.example.playlist.playlist.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Playlist {
    private Long id;
    private Long memberId;
    private String spotifyPlaylistId;
    private String name;
    private String prompt;
    private LocalDateTime createdAt;
}