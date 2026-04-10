package com.example.playlist.gemini.exception;

import com.example.playlist.global.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GeminiErrorCode implements ErrorCode {

    GEMINI_IS_BUSY(HttpStatus.BAD_REQUEST, "AI 요청이 많아 잠시 후에 시도해주세요.");

    private final HttpStatus status;
    private final String message;

    GeminiErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
