package com.example.playlist.playlist.exception;

import com.example.playlist.global.error.BaseException;

public class PlaylistException extends BaseException {

    public PlaylistException(PlaylistErrorCode errorCode) {
        super(errorCode);
    }
}
