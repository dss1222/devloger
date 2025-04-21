package com.devloger.authservice.dto;

public record UserMeResponse(
    Long id,
    String email,
    String nickname
) {}
