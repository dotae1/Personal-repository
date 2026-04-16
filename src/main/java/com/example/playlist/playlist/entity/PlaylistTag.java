package com.example.playlist.playlist.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaylistTag {
    private Long playlistId;
    private Long tagId;
}