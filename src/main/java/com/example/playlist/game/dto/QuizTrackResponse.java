package com.example.playlist.game.dto;

import com.example.playlist.spotify.dto.SpotifySearchResponse;

import java.util.List;

public record QuizTrackResponse(
        String trackId,
        String previewUrl,
        String title,
        String artist,
        String albumImageUrl
) {
    public static QuizTrackResponse from(SpotifySearchResponse.Item item) {
        String artist = item.getArtists() != null && !item.getArtists().isEmpty()
                ? item.getArtists().get(0).getName()
                : "Unknown";

        String imageUrl = null;
        List<SpotifySearchResponse.AlbumImage> images = item.getAlbum().getImages();
        if (images != null && !images.isEmpty()) {
            imageUrl = images.get(0).getUrl();
        }

        return new QuizTrackResponse(
                item.getTrackId(),
                item.getPreviewUrl(),
                item.getName(),
                artist,
                imageUrl
        );
    }
}
