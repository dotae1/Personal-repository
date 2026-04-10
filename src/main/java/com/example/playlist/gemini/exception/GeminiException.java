package com.example.playlist.gemini.exception;

import com.example.playlist.global.error.BaseException;

public class GeminiException extends BaseException {
    public GeminiException(GeminiErrorCode errorCode) {super(errorCode);}
}
