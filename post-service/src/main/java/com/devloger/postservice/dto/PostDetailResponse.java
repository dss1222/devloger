package com.devloger.postservice.dto;

import com.devloger.postservice.domain.Post;

import java.time.LocalDateTime;

public record PostDetailResponse(
    Long id,
    String title,
    String content,
    Long userId,
    LocalDateTime createdAt
) {
    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getUserId(),
            post.getCreatedAt()
        );
    }
}
