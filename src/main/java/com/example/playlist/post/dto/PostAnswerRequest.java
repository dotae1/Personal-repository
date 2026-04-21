package com.example.playlist.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PostAnswerRequest {

    @NotBlank(message = "답변 내용은 필수로 입력해야 합니다.")
    private String answer;
}
