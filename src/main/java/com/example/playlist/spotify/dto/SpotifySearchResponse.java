package com.example.playlist.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class SpotifySearchResponse {
    private Tracks tracks;

    @Getter
    public static class Tracks {
        private List<Item> items;
    }

    @Getter
    public static class Item {
        @JsonProperty("id")
        private String trackId;
        @JsonProperty("uri")
        private String trackUri;

        private String name;

        private List<Artist> artists;

        private Album album;
    }

    @Getter
    public static class Artist {
        private String name;
    }

    @Getter
    public static class Album {
        private String name;

        @JsonProperty("images")
        private List<AlbumImage> images;
    }

    @Getter
    public static class AlbumImage {
        private String url;
    }
}
