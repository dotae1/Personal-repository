package com.example.playlist.post.exception;

import com.example.playlist.global.success.SuccessCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PostSuccessCode implements SuccessCode {

    POST_CREATED(HttpStatus.CREATED, "문의가 등록되었습니다."),
    POST_LIST(HttpStatus.OK, "문의 목록 조회 성공"),
    POST_DETAIL(HttpStatus.OK, "문의 상세 조회 성공"),
    POST_ANSWERED(HttpStatus.OK, "답변이 등록되었습니다.");

    private final HttpStatus status;
    private final String message;

    PostSuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
