package com.example.playlist.Entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaylistSong {
    private Long id;
    private Long playlistId;
    private Long songId;
    private Integer position;
}