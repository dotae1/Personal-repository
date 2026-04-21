package com.example.playlist.playlist.dto;

import com.example.playlist.playlist.entity.Playlist;
import com.example.playlist.playlist.entity.Song;

import java.time.LocalDateTime;
import java.util.List;

public record PlaylistDetailResponse(
        Long id,
        String name,
        String prompt,
        LocalDateTime createdAt,
        List<SongResponse> songs
) {
    public record SongResponse(
            Long id,
            String title,
            String artist,
            String album,
            String spotifyTrackId,
            String spotifyTrackUri
    ) {
        public static SongResponse from(Song song) {
            return new SongResponse(
                    song.getId(),
                    song.getTitle(),
                    song.getArtist(),
                    song.getAlbum(),
                    song.getSpotifyTrackId(),
                    song.getSpotifyTrackUri()
            );
        }
    }

    public static PlaylistDetailResponse of(Playlist playlist, List<Song> songs) {
        return new PlaylistDetailResponse(
                playlist.getId(),
                playlist.getName(),
                playlist.getPrompt(),
                playlist.getCreatedAt(),
                songs.stream().map(SongResponse::from).toList()
        );
    }
}
