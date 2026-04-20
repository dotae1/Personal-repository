package com.example.playlist.playlist.repository;

import com.example.playlist.playlist.entity.Playlist;
import com.example.playlist.playlist.entity.PlaylistSong;
import com.example.playlist.playlist.entity.Song;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PlaylistMapper {

    void insertPlaylist(Playlist playlist);

    void insertSong(Song song);

    void insertPlaylistSong(PlaylistSong playlistSong);

    Optional<Song> findSongBySpotifyTrackId(@Param("spotifyTrackId") String spotifyTrackId);

    List<Playlist> findByMemberId(@Param("memberId") Long memberId);
}
