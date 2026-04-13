package com.example.playlist.spotify.dto;

import lombok.Getter;

@Getter
public class SpotifyTrack {

    private String trackId;
    private String trackUri;
    private String title;
    private String artist;
    private String album;
    private String albumImageUrl;

    private SpotifyTrack(String trackId, String trackUri, String title, String artist, String album, String albumImageUrl) {
        this.trackId = trackId;
        this.trackUri = trackUri;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.albumImageUrl = albumImageUrl;
    }


    public static SpotifyTrack from(SpotifySearchResponse.Item item, String displayTitle, String displayArtist) {
        return new SpotifyTrack(
                item.getTrackId(),
                item.getTrackUri(),
                displayTitle,
                displayArtist,
                item.getAlbum().getName(),
                item.getAlbum().getImages().get(0).getUrl()
        );
    }
}
