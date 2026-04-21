package com.example.playlist.post.dto;

import com.example.playlist.post.entity.InquiryCategory;
import com.example.playlist.post.entity.InquiryStatus;
import com.example.playlist.post.entity.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        Long memberId,
        String title,
        String content,
        InquiryCategory category,
        InquiryStatus status,
        String answer,
        LocalDateTime answeredAt,
        LocalDateTime createdAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getMemberId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getStatus(),
                post.getAnswer(),
                post.getAnsweredAt(),
                post.getCreatedAt()
        );
    }
}
