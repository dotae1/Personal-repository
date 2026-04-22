package com.example.playlist.game.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ItunesSearchResponse {

    private int resultCount;
    private List<ItunesTrack> results;

    @Getter
    public static class ItunesTrack {
        private Long trackId;
        private String trackName;
        private String artistName;
        private String previewUrl;
        private String artworkUrl100;

        public String getArtworkUrl() {
            if (artworkUrl100 == null) return null;
            return artworkUrl100.replace("100x100bb", "500x500bb");
        }
    }
}
