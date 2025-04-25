package com.devloger.postservice.dto;

import jakarta.validation.constraints.NotBlank;

public record PostUpdateRequest(
    @NotBlank(message = "제목은 필수 입력값입니다.")
    String title,
    
    @NotBlank(message = "내용은 필수 입력값입니다.")
    String content
) {}