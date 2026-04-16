package com.example.playlist.mail.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MailVerificationRequest {

    private String email;
    @NotNull @Size(min = 6, max = 6)
    private String code;

}
