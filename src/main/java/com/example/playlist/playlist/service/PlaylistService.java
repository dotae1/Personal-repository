package com.example.playlist.playlist.service;

import com.example.playlist.gemini.dto.GeminiRequest;
import com.example.playlist.gemini.dto.GeminiResponse;
import com.example.playlist.gemini.service.GeminiService;
import com.example.playlist.member.entity.Member;
import com.example.playlist.member.repository.MemberMapper;
import com.example.playlist.member.repository.MemberSocialMapper;
import com.example.playlist.member.oauth2.CustomOAuth2UserService;
import com.example.playlist.playlist.repository.PlaylistMapper;
import com.example.playlist.playlist.exception.PlaylistErrorCode;
import com.example.playlist.playlist.exception.PlaylistException;
import com.example.playlist.playlist.dto.PlaylistDetailResponse;
import com.example.playlist.playlist.dto.PlaylistResponse;
import com.example.playlist.playlist.dto.SaveNewPlaylistRequest;
import com.example.playlist.playlist.dto.SaveToExistingPlaylistRequest;
import com.example.playlist.playlist.dto.SpotifyTrackSaveRequest;
import com.example.playlist.playlist.entity.Playlist;
import com.example.playlist.playlist.entity.PlaylistSong;
import com.example.playlist.playlist.entity.Song;
import com.example.playlist.spotify.dto.SpotifyTrack;
import com.example.playlist.spotify.dto.SpotifyUserPlaylistsResponse;
import com.example.playlist.spotify.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final GeminiService geminiService;
    private final SpotifyService spotifyService;
    private final PlaylistMapper playlistMapper;
    private final MemberMapper memberMapper;
    private final MemberSocialMapper memberSocialMapper;
    private final RedisTemplate<String, String> redisTemplate;

    /** AI 추천 플레이리스트 생성 (DB 미저장, 결과만 반환) */
    public PlaylistResponse createPlayList(GeminiRequest request) throws com.fasterxml.jackson.core.JsonProcessingException {
        GeminiResponse geminiResponse = geminiService.CreatePlaylist(request);

        List<SpotifyTrack> tracks = Flux.fromIterable(geminiResponse.getSongs())
                .flatMap(song -> spotifyService.searchTrack(song.getArtist(), song.getTitle()))
                .collectList()
                .block();

        return new PlaylistResponse(geminiResponse.getPlaylistTitle(), tracks);
    }

    /** 유저의 Spotify 플레이리스트 목록 반환 */
    public SpotifyUserPlaylistsResponse getUserSpotifyPlaylists(String loginId) {
        String userToken = getSpotifyUserToken(loginId);
        return spotifyService.getUserPlaylists(userToken);
    }

    /** 새 Spotify 플레이리스트 생성 후 트랙 추가 + DB 저장 */
    public void saveAsNewPlaylist(String loginId, SaveNewPlaylistRequest req) {
        Long memberId = getMemberId(loginId);
        String userToken = getSpotifyUserToken(loginId);

        String spotifyPlaylistId = spotifyService.createPlaylist(req.getPlaylistName(), userToken);
        log.info("[Playlist] 새 플레이리스트 생성됨 - spotifyPlaylistId={}", spotifyPlaylistId);

        List<String> trackUris = req.getTracks().stream()
                .map(SpotifyTrackSaveRequest::getSpotifyTrackUri)
                .toList();
        spotifyService.addTracksToPlaylist(spotifyPlaylistId, trackUris, userToken);

        savePlaylistToDB(memberId, spotifyPlaylistId, req.getPlaylistName(), req.getPrompt(), req.getTracks());
        log.info("[Playlist] 새 플레이리스트 생성 완료 - memberId={}, spotifyPlaylistId={}", memberId, spotifyPlaylistId);
    }

    /** 기존 Spotify 플레이리스트에 트랙 추가 + DB 저장 */
    public void saveToExistingPlaylist(String loginId, SaveToExistingPlaylistRequest req) {
        Long memberId = getMemberId(loginId);
        String userToken = getSpotifyUserToken(loginId);

        List<String> trackUris = req.getTracks().stream()
                .map(SpotifyTrackSaveRequest::getSpotifyTrackUri)
                .toList();
        spotifyService.addTracksToPlaylist(req.getTargetSpotifyPlaylistId(), trackUris, userToken);

        savePlaylistToDB(memberId, req.getTargetSpotifyPlaylistId(), req.getPlaylistName(), req.getPrompt(), req.getTracks());
        log.info("[Playlist] 기존 플레이리스트에 추가 완료 - memberId={}, spotifyPlaylistId={}", memberId, req.getTargetSpotifyPlaylistId());
    }

    /** 플레이리스트 상세 조회 (곡 목록 포함) */
    public PlaylistDetailResponse getPlaylistDetail(String loginId, Long playlistId) {
        Long memberId = getMemberId(loginId);

        Playlist playlist = playlistMapper.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        if (!playlist.getMemberId().equals(memberId)) {
            throw new PlaylistException(PlaylistErrorCode.PLAYLIST_FORBIDDEN);
        }

        List<Song> songs = playlistMapper.findSongsByPlaylistId(playlistId);
        return PlaylistDetailResponse.of(playlist, songs);
    }

    /** 플레이리스트 삭제 */
    public void deletePlaylist(String loginId, Long playlistId) {
        Long memberId = getMemberId(loginId);

        playlistMapper.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        playlistMapper.deleteById(playlistId, memberId);
        log.info("[Playlist] 삭제 완료 - memberId={}, playlistId={}", memberId, playlistId);
    }

    // ─────────────────────────────────────────────
    // 내부 헬퍼
    // ─────────────────────────────────────────────

    private void savePlaylistToDB(Long memberId, String spotifyPlaylistId, String name, String prompt,
                                  List<SpotifyTrackSaveRequest> tracks) {
        Playlist playlist = Playlist.builder()
                .memberId(memberId)
                .spotifyPlaylistId(spotifyPlaylistId)
                .name(name)
                .prompt(prompt)
                .build();
        playlistMapper.insertPlaylist(playlist);

        for (int i = 0; i < tracks.size(); i++) {
            SpotifyTrackSaveRequest t = tracks.get(i);

            Song song = playlistMapper.findSongBySpotifyTrackId(t.getSpotifyTrackId())
                    .orElseGet(() -> {
                        Song newSong = Song.builder()
                                .spotifyTrackId(t.getSpotifyTrackId())
                                .spotifyTrackUri(t.getSpotifyTrackUri())
                                .title(t.getTitle())
                                .artist(t.getArtist())
                                .album(t.getAlbum())
                                .build();
                        playlistMapper.insertSong(newSong);
                        return newSong;
                    });

            PlaylistSong ps = PlaylistSong.builder()
                    .playlistId(playlist.getId())
                    .songId(song.getId())
                    .position(i + 1)
                    .build();
            playlistMapper.insertPlaylistSong(ps);
        }
    }

    private Long getMemberId(String loginId) {
        return memberMapper.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."))
                .getId();
    }

    private String getSpotifyUserToken(String loginId) {
        Long memberId = getMemberId(loginId);
        String token = redisTemplate.opsForValue().get(CustomOAuth2UserService.SPOTIFY_USER_TOKEN_KEY + memberId);
        if (token == null) {
            throw new PlaylistException(PlaylistErrorCode.SPOTIFY_NOT_CONNECTED);
        }
        return token;
    }
}
