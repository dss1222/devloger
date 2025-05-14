package com.devloger.postservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorCode {
    VALIDATION_ERROR("VALIDATION_ERROR", "유효성 검사에 실패했습니다.", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND("POST_NOT_FOUND", "존재하지 않는 게시글입니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", "수정 권한이 없습니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}

