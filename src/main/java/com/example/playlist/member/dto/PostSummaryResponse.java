package com.example.playlist.member.dto;

import com.example.playlist.post.entity.InquiryCategory;
import com.example.playlist.post.entity.InquiryStatus;
import com.example.playlist.post.entity.Post;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String title,
        InquiryCategory category,
        InquiryStatus status,
        LocalDateTime createdAt
) {
    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory(),
                post.getStatus(),
                post.getCreatedAt()
        );
    }
}
