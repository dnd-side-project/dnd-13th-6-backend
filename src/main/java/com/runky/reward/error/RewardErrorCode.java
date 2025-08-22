package com.runky.reward.error;

import com.runky.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum RewardErrorCode implements ErrorCode {
    INVALID_BADGE_IMAGE_URL(HttpStatus.BAD_REQUEST, "RE000", "배지 이미지 URL이 유효하지 않습니다."),
    INVALID_BADGE_NAME(HttpStatus.BAD_REQUEST, "RE001", "배지 이름이 유효하지 않습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
