package com.example.playlist.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class SpotifyCurrentUserResponse {

    private String id;

    @JsonProperty("display_name")
    private String displayName;

    private String email;
}
