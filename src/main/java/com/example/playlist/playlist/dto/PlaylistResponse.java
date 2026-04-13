package com.example.playlist.playlist.dto;

import com.example.playlist.spotify.dto.SpotifyTrack;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@Getter
@AllArgsConstructor
public class PlaylistResponse {
    private final String playlistTitle;
    private final List<SpotifyTrack> tracks;
}
