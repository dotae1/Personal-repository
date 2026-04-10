package com.example.playlist.global.handler;

import com.example.playlist.global.error.BaseException;
import com.example.playlist.global.error.ErrorCode;
import com.example.playlist.global.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e, HttpServletRequest request) {
        return getErrorResponse(e, e.getErrorCode(), request);
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

