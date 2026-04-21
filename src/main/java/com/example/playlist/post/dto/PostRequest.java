package com.example.playlist.post.dto;

import com.example.playlist.post.entity.InquiryCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PostRequest {

    @NotBlank(message = "제목은 필수로 입력해야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수로 입력해야 합니다.")
    private String content;

    @NotNull(message = "카테고리는 필수로 선택해야 합니다.")
    private InquiryCategory category;
}
