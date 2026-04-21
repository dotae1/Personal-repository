package com.example.playlist.member.exception;

import com.example.playlist.global.success.SuccessCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberSuccessCode implements SuccessCode {

    LOGIN_SUCCESS(HttpStatus.OK, "로그인이 성공적으로 완료되었습니다."),
    MEMBER_SUCCESS_SIGNUP(HttpStatus.OK, "회원가입이 성공적으로 완료되었습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃이 성공적으로 완료되었습니다."),
    PROFILE_COMPLETE_SUCCESS(HttpStatus.OK, "추가 정보 입력이 완료되었습니다."),
    MYPAGE_SUCCESS(HttpStatus.OK, "마이페이지 조회 성공");

    private final HttpStatus status;
    private final String message;

    MemberSuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
