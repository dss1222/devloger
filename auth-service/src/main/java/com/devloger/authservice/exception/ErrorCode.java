package com.devloger.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_EMAIL("INVALID_EMAIL", "유효하지 않은 이메일 형식입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATED_EMAIL("DUPLICATED_EMAIL", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    INVALID_PASSWORD("INVALID_PASSWORD", "비밀번호는 최소 8자 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    DUPLICATED_NICKNAME("DUPLICATED_NICKNAME", "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),
    INVALID_NICKNAME("INVALID_NICKNAME", "닉네임은 2자 이상이어야 합니다.", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("USER_NOT_FOUND", "해당 사용자가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
