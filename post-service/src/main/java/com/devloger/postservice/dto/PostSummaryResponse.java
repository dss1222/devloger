package com.devloger.postservice.dto;

import java.time.LocalDateTime;

public record PostSummaryResponse(
    Long id,
    String title,
    LocalDateTime createdAt
) {}