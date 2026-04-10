package com.example.playlist.global.success;

import com.fasterxml.jackson.annotation.JsonInclude;

public record SuccessResponse<T>(
        int status,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        T result
) {
    public static <T> SuccessResponse<T> of(SuccessCode successCode) {
        return new SuccessResponse<>(successCode.getStatus().value(), successCode.getMessage(), null);
    }

    public static <T> SuccessResponse<T> of(SuccessCode successCode, T result) {
        return new SuccessResponse<>(successCode.getStatus().value(), successCode.getMessage(), result);
    }
}

