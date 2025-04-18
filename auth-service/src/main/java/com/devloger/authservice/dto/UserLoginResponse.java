package com.devloger.authservice.dto;

public record UserLoginResponse(
    Long id,
    String email,
    String nickname,
    String accessToken
) {}
