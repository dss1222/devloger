package com.devloger.postservice.dto;

import java.time.LocalDateTime;

public record PostCreateResponse(
    Long id,
    String title,
    String content,
    Long userId,
    LocalDateTime createdAt
) {}