package com.example.playlist.Entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaylistTag {
    private Long playlistId;
    private Long tagId;
}