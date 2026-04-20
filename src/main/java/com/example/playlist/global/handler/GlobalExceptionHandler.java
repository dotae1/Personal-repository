package com.example.playlist.global.handler;

import com.example.playlist.global.error.BaseException;
import com.example.playlist.global.error.ErrorCode;
import com.example.playlist.global.error.ErrorResponse;
import com.example.playlist.playlist.exception.PlaylistErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e, HttpServletRequest request) {
        return getErrorResponse(e, e.getErrorCode(), request);
    }

    /**
     * Spotify API 호출 실패 처리
     * - 401: 토큰 만료 → 재로그인 안내
     * - 403: 권한 부족 → 재로그인(스코프 재동의) 안내
     * - 그 외: 일반 오류
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponseException(
            WebClientResponseException e, HttpServletRequest request) {
        log.error("[WebClientResponseException] status={}, Request: {} {}\nSpotify 응답 본문: {}",
                e.getStatusCode(), request.getMethod(), request.getRequestURI(),
                e.getResponseBodyAsString());

        PlaylistErrorCode errorCode;
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            errorCode = PlaylistErrorCode.SPOTIFY_TOKEN_EXPIRED;
        } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
            errorCode = PlaylistErrorCode.SPOTIFY_FORBIDDEN;
        } else {
            errorCode = PlaylistErrorCode.SPOTIFY_API_ERROR;
        }

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        return getErrorResponse(e, GlobalErrorCode.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        return getErrorResponse(e, GlobalErrorCode.INTERNAL_SERVER_ERROR, request);
    }

    private static ResponseEntity<ErrorResponse> getErrorResponse(Exception e, GlobalErrorCode errorCode, HttpServletRequest request) {
        log.error(
                "[GlobalException] message: {}, Request: {} {}",
                e.getMessage(),
                request.getMethod(),
                request.getRequestURI(),
                e
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    private static ResponseEntity<ErrorResponse> getErrorResponse(BaseException e, ErrorCode errorCode, HttpServletRequest request) { // BaseException을 받도록 명확히 합니다.
        log.warn(
                "[BaseException] message: {}, Request: {} {}",
                errorCode.getMessage(),
                request.getMethod(),
                request.getRequestURI(),
                e
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, e.getMessage()));
    }
}

