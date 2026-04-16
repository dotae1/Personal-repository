package com.example.playlist.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LoginRequest {

    @NotNull(message = "아이디는 필수로 입력하셔야 합니다.")
    private String loginId;
    @NotNull(message = "비밀번호는 필수로 입력하셔야 합니다.")
    private String password;
}
