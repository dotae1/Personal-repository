package com.example.playlist.game.dto;

import com.example.playlist.game.entity.QuizTrack;

public record QuizTrackResponse(
        String trackId,
        String previewUrl,
        String title,
        String artist,
        String albumImageUrl,
        String titleNormalized,
        String artistNormalized
) {
    public static QuizTrackResponse fromEntity(QuizTrack track) {
        return new QuizTrackResponse(
                track.getItunesTrackId(),
                track.getPreviewUrl(),
                track.getTitle(),
                track.getArtist(),
                track.getAlbumImageUrl(),
                normalize(track.getTitle()),
                normalize(track.getArtist())
        );
    }

    private static String normalize(String value) {
        if (value == null) return null;
        return value.replaceAll("\\s+", "").toLowerCase();
    }
}
