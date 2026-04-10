package com.example.playlist.gemini.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class GeminiResponse {
    private String playlistTitle;
    private List<SongInfo> songs;

    @Getter
    @NoArgsConstructor
    public static class SongInfo {
        private String title;
        private String artist;
    }
}
