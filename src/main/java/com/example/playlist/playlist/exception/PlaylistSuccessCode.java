package com.example.playlist.playlist.exception;

import com.example.playlist.global.success.SuccessCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PlaylistSuccessCode implements SuccessCode {

    PLAYLIST_SAVED(HttpStatus.CREATED, "플레이리스트가 저장되었습니다."),
    PLAYLIST_ADDED(HttpStatus.OK, "기존 플레이리스트에 트랙이 추가되었습니다.");

    private final HttpStatus status;
    private final String message;

    PlaylistSuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
