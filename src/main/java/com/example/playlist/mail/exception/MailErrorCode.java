package com.example.playlist.mail.exception;

import com.example.playlist.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum MailErrorCode implements ErrorCode {

    MAIL_ERROR_CODE(HttpStatus.BAD_REQUEST, "이메일 오류 발생"),
    CODE_INVALID(HttpStatus.BAD_REQUEST, "인증코드 만료되었습니다."),
    CODE_IS_NOT_CORRECT(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    CODE_OK(HttpStatus.OK, "인증 성공"),
    EMAIL_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 가입되어있는 이메일입니다. 해당 이메일로 로그인해주세요.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return "[MAIL ERROR]" + message;
    }
}

