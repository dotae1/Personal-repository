package com.example.playlist.game.repository;

import com.example.playlist.game.entity.QuizTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuizTrackMapper {
    void insert(QuizTrack track);
    boolean existsBySpotifyTrackId(@Param("spotifyTrackId") String spotifyTrackId);
    QuizTrack findRandomByDecade(@Param("decade") int decade);
    int countByDecade(@Param("decade") int decade);
}
