package com.devloger.postservice.dto;

import jakarta.validation.constraints.NotBlank;

public record PostCreateRequest(
    @NotBlank String title,
    @NotBlank String content
) {}
