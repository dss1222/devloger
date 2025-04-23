package com.devloger.postservice.dto;

import java.time.LocalDateTime;

import com.devloger.postservice.domain.Post;

public record PostSummaryResponse(
    Long id,
    String title,
    String content,
    Long userId,
    LocalDateTime createdAt
) {
    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getUserId(),
            post.getCreatedAt()
        );
    }
}