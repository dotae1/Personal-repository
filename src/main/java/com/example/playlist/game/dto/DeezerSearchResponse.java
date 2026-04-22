package com.example.playlist.game.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class DeezerSearchResponse {

    private List<DeezerTrack> data;

    @Getter
    public static class DeezerTrack {
        private String preview;
    }
}
