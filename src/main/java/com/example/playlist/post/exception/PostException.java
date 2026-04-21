package com.example.playlist.post.exception;

import com.example.playlist.global.error.BaseException;

public class PostException extends BaseException {
    public PostException(PostErrorCode errorCode) {
        super(errorCode);
    }
}