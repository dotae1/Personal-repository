package com.example.playlist.playlist.service;

import com.example.playlist.gemini.dto.GeminiRequest;
import com.example.playlist.gemini.dto.GeminiResponse;
import com.example.playlist.gemini.service.GeminiService;
import com.example.playlist.playlist.dto.PlaylistResponse;
import com.example.playlist.spotify.dto.SpotifyTrack;
import com.example.playlist.spotify.service.SpotifyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final GeminiService geminiService;
    private final SpotifyService spotifyService;

    public PlaylistResponse createPlayList(GeminiRequest request) throws JsonProcessingException {
        GeminiResponse geminiResponse = geminiService.CreatePlaylist(request);

        List<SpotifyTrack> tracks = Flux.fromIterable(geminiResponse.getSongs())
                .flatMap(song -> spotifyService.searchTrack(song.getArtist(), song.getTitle()))
                .collectList()
                .block();

        return new PlaylistResponse(geminiResponse.getPlaylistTitle(), tracks);
    }
}
