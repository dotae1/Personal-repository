package com.example.playlist.spotify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class SpotifyUserPlaylistsResponse {

    private List<PlaylistItem> items;

    @Getter
    public static class PlaylistItem {
        private String id;
        private String name;
        private List<Image> images;
        private Owner owner;

        @JsonProperty("tracks")
        private TracksInfo tracks;
    }

    @Getter
    public static class Owner {
        private String id;

        @JsonProperty("display_name")
        private String displayName;
    }

    @Getter
    public static class Image {
        private String url;
    }

    @Getter
    public static class TracksInfo {
        private int total;
    }
}
