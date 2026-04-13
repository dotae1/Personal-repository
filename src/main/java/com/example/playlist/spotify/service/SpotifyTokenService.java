package com.example.playlist.spotify.service;

import com.example.playlist.spotify.dto.SpotifyTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class SpotifyTokenService {

    private final WebClient spotifyAccountsClient;

    @Value("${spotify.client-id}")
    private String clientId;
    @Value("${spotify.client-secret}")
    private String clientSecret;

    private final AtomicReference<String> cachedToken = new AtomicReference<>();
    private volatile Instant tokenExpiresAt = Instant.MIN;

    @PostConstruct
    public void init() {
        refreshToken();
    }

    public String getAccessToken() {
        if(Instant.now().isBefore(tokenExpiresAt.minusSeconds(300))) {
            return cachedToken.get();
        }
        return refreshToken();
    }

    private String refreshToken() {
        String credentials = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        SpotifyTokenResponse response = spotifyAccountsClient
                .post()
                .uri("/api/token")
                .header("Authorization", "Basic " + encoded)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials")
                .retrieve()
                .bodyToMono(SpotifyTokenResponse.class)
                .block();

        cachedToken.set(response.getAccessToken());
        tokenExpiresAt = Instant.now().plusSeconds(response.getExpiresIn());

        return response.getAccessToken();
    }

    @Scheduled(fixedDelay = 50 * 60 * 1000)
    public void scheduleTokenRefresh() {
        refreshToken();
    }
}
