package com.example.playlist.game.dto;

import com.example.playlist.game.entity.QuizTrack;

public record QuizTrackResponse(
        String trackId,
        String previewUrl,
        String title,
        String artist,
        String albumImageUrl,
        String titleNormalized,        // 한국어 제목 정규화 (괄호 제거 + 공백 제거 + 소문자)
        String titleNormalizedAlias,   // 영어 제목 정규화 (null 가능)
        String artistNormalized,       // 한국어 아티스트 정규화
        String artistNormalizedAlias   // 영어 아티스트 정규화 (null 가능)
) {
    public static QuizTrackResponse fromEntity(QuizTrack track) {
        return new QuizTrackResponse(
                track.getItunesTrackId(),
                track.getPreviewUrl(),
                track.getTitle(),
                track.getArtist(),
                track.getAlbumImageUrl(),
                normalize(track.getTitle()),
                normalize(track.getTitleAlias()),
                normalize(track.getArtist()),
                normalize(track.getArtistAlias())
        );
    }

    private static String normalize(String value) {
        if (value == null) return null;
        return value
                .replaceAll("\\(.*?\\)", "")   // (영어 부제) 제거
                .replaceAll("\\[.*?\\]", "")   // [영어 부제] 제거
                .replaceAll("\\s+", "")
                .toLowerCase()
                .trim();
    }
}
