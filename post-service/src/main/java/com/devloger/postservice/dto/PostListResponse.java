package com.devloger.postservice.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record PostListResponse(
    List<PostSummaryResponse> posts,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean isLast
) {
    public static PostListResponse from(Page<PostSummaryResponse> page) {
        return new PostListResponse(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}