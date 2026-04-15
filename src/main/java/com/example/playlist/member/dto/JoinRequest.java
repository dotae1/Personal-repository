package com.example.playlist.member.dto;

import com.example.playlist.member.entity.Gender;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JoinRequest {

    @NotNull(message = "아이디는 필수로 입력하여야 합니다.")
    private String loginId;
    @NotNull(message = "이메일은 필수로 입력하셔야 합니다.")
    private String email;
    @NotNull(message = "이름은 필수로 입력하셔야 합니다.")
    private String name;
    @NotNull(message = "닉네임은 필수로 입력하셔야 합니다.")
    private String nickname;
    @NotNull(message = "비밀번호는 필수로 입력하셔야 합니다.")
    private String password;
    private Gender gender;
    private int age;
}
