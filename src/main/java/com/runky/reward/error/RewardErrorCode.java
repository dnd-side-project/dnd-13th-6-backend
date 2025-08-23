package com.runky.reward.error;

import com.runky.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum RewardErrorCode implements ErrorCode {
    INVALID_BADGE_IMAGE_URL(HttpStatus.BAD_REQUEST, "RE000", "배지 이미지 URL이 유효하지 않습니다."),
    INVALID_BADGE_NAME(HttpStatus.BAD_REQUEST, "RE001", "배지 이름이 유효하지 않습니다."),
    INSUFFICIENT_CLOVER(HttpStatus.CONFLICT, "RE002", "클로버가 부족합니다."),
    NOT_FOUND_CLOVER(HttpStatus.NOT_FOUND, "RE003", "사용자의 클로버를 찾을 수 없습니다."),
    INVALID_CLOVER_ADD_REQUEST(HttpStatus.BAD_REQUEST, "RE004", "추가할 클로버 수량은 양수이어야 합니다."),
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
