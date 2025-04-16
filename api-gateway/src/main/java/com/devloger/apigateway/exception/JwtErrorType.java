package com.devloger.apigateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum JwtErrorType {

    EXPIRED("JWT_EXPIRED", "Token has expired", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED("JWT_UNSUPPORTED", "Unsupported JWT", HttpStatus.BAD_REQUEST),
    MALFORMED("JWT_MALFORMED", "Invalid JWT format", HttpStatus.BAD_REQUEST),
    SIGNATURE("JWT_INVALID_SIGNATURE", "JWT signature does not match", HttpStatus.UNAUTHORIZED),
    GENERAL("JWT_INVALID", "JWT validation failed", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;

    JwtErrorType(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
