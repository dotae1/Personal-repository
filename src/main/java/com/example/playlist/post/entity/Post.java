package com.example.playlist.post.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Post {
    private Long id;
    private Long memberId;
    private String title;
    private String content;
    private InquiryCategory category;
    private InquiryStatus status;
    private String answer;
    private LocalDateTime answeredAt;
    private LocalDateTime createdAt;
}
