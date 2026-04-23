package com.example.playlist.game.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizTrack {
    private Long id;
    private String title;
    private String artist;
    private String titleAlias;   // 영어 제목 (iTunes trackName)
    private String artistAlias;  // 영어 아티스트명 (iTunes artistName)
    private String albumImageUrl;
    private String previewUrl;
    private String decade;
    private String itunesTrackId;
    private LocalDateTime createdAt;
}
