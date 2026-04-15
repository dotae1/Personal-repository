package com.example.playlist.member.exception;

import com.example.playlist.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    INVALID_MEMBER_ID(HttpStatus.BAD_REQUEST, "입력하신 회원 ID가 올바르지 않거나 존재하지 않습니다."),
    MEMBER_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 회원가입이 되어있습니다."),
    ACCESS_EXCEPTION(HttpStatus.BAD_REQUEST, "액세스 토큰이 없습니다."),
    MAIL_VERIFIED_FAILED(HttpStatus.BAD_REQUEST, "메일 인증이 완료되지않았습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return "[AUTH ERROR]" + message;
    }
}