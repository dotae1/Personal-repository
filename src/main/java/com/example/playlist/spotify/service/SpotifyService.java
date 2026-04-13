package com.example.playlist.spotify.service;

import com.example.playlist.spotify.dto.SpotifySearchResponse;
import com.example.playlist.spotify.dto.SpotifyTrack;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final SpotifyTokenService spotifyTokenService;
    private final WebClient spotifyWebClient;

    public SpotifyTrack searchTrack(String artist, String title) {
        String accessToken = spotifyTokenService.getAccessToken();

        SpotifySearchResponse response = spotifyWebClient
                .get()
                .uri("/search?q={q}&type=track&limit=1&market=KR", artist + " " + title)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(SpotifySearchResponse.class)
                .block();

        return SpotifyTrack.from(response.getTracks().getItems().get(0), title, artist);
    }
}
