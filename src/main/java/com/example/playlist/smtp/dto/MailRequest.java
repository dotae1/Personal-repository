package com.example.playlist.smtp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MailRequest {
    @NotBlank(message = "이메일은 필수로 입력하여야 합니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
}
