package com.devloger.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserSignupRequest(
    @Email
    @NotBlank
    String email,

    @NotBlank
    String password,

    String nickname
) {}
