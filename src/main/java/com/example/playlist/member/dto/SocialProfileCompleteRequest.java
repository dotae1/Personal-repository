package com.example.playlist.member.dto;

import com.example.playlist.member.entity.Gender;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialProfileCompleteRequest {

    @NotBlank(message = "닉네임은 필수로 입력하여야 합니다.")
    private String nickname;

    @NotNull(message = "성별은 필수로 입력하여야 합니다.")
    private Gender gender;

    @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
    private int age;
}
