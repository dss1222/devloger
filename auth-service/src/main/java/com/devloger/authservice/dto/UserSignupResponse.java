package com.devloger.authservice.dto;

import lombok.Builder;

@Builder
public record UserSignupResponse(
        Long id,
        String email,
        String nickname
) {}