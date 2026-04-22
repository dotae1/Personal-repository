package com.example.playlist.game.repository;

import com.example.playlist.game.entity.QuizTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuizTrackMapper {
    void insert(QuizTrack track);
    boolean existsByItunesTrackId(@Param("itunesTrackId") String itunesTrackId);
    List<String> findTitlesByDecade(@Param("decade") String decade);
    QuizTrack findRandomByDecade(@Param("decade") String decade);
    int countByDecade(@Param("decade") String decade);
}
