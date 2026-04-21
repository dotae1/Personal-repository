package com.example.playlist.playlist.exception;

import com.example.playlist.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PlaylistErrorCode implements ErrorCode {

    PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "플레이리스트를 찾을 수 없습니다."),
    PLAYLIST_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 플레이리스트만 삭제할 수 있습니다."),
    SPOTIFY_NOT_CONNECTED(HttpStatus.BAD_REQUEST,
            "Spotify 연동이 필요합니다. Spotify 소셜 로그인을 먼저 진행해주세요."),
    SPOTIFY_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST,
            "Spotify 토큰이 만료되었거나 권한이 부족합니다. Spotify 소셜 로그인을 다시 진행해주세요."),
    SPOTIFY_FORBIDDEN(HttpStatus.FORBIDDEN,
            "Spotify 플레이리스트 수정 권한이 없습니다. 본인이 소유한 플레이리스트만 수정할 수 있습니다."),
    SPOTIFY_API_ERROR(HttpStatus.BAD_GATEWAY,
            "Spotify API 호출 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String message;
}
