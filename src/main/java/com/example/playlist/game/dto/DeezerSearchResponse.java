package com.example.playlist.game.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class DeezerSearchResponse {

    private List<DeezerTrack> data;

    @Getter
    public static class DeezerTrack {
        private String preview;
        private DeezerAlbum album;

        public String getAlbumImageUrl() {
            return album != null ? album.getCoverBig() : null;
        }
    }

    @Getter
    public static class DeezerAlbum {
        @JsonProperty("cover_big")
        private String coverBig;
    }
}
