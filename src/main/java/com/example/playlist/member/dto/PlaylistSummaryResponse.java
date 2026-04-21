package com.example.playlist.member.dto;

import com.example.playlist.playlist.entity.Playlist;

import java.time.LocalDateTime;

public record PlaylistSummaryResponse(
        Long id,
        String name,
        String prompt,
        LocalDateTime createdAt
) {
    public static PlaylistSummaryResponse from(Playlist playlist) {
        return new PlaylistSummaryResponse(
                playlist.getId(),
                playlist.getName(),
                playlist.getPrompt(),
                playlist.getCreatedAt()
        );
    }
}
