package com.devloger.postservice.util;

import com.devloger.postservice.domain.Post;
import com.devloger.postservice.dto.PostCreateRequest;
import com.devloger.postservice.dto.PostCreateResponse;
import com.devloger.postservice.dto.PostDetailResponse;
import com.devloger.postservice.dto.PostSummaryResponse;

import java.time.LocalDateTime;

public class TestDataUtil {
    // 테스트 데이터 상수
    public static final String TEST_TITLE = "테스트 제목";
    public static final String TEST_CONTENT = "테스트 내용";
    public static final Long TEST_USER_ID = 1L;
    public static final Long TEST_POST_ID = 1L;
    public static final LocalDateTime TEST_CREATED_AT = LocalDateTime.now();

    // 테스트 데이터 생성 메서드
    public static PostCreateRequest createPostRequest(String title, String content) {
        return new PostCreateRequest(title, content);
    }

    public static Post createPost(Long id, String title, String content, Long userId, LocalDateTime createdAt) {
        return Post.builder()
                .id(id)
                .title(title)
                .content(content)
                .userId(userId)
                .createdAt(createdAt)
                .build();
    }

    public static PostCreateResponse createPostResponse(Long id, String title, String content, Long userId, LocalDateTime createdAt) {
        return new PostCreateResponse(id, title, content, userId, createdAt);
    }

    public static PostDetailResponse createPostDetailResponse(Long id, String title, String content, Long userId, LocalDateTime createdAt) {
        return new PostDetailResponse(id, title, content, userId, createdAt);
    }

    public static PostSummaryResponse createPostSummaryResponse(Long id, String title, String content, Long userId, LocalDateTime createdAt) {
        return new PostSummaryResponse(id, title, content, userId, createdAt);
    }
} 