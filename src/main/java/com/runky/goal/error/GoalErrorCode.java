package com.runky.goal.error;

import com.runky.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum GoalErrorCode implements ErrorCode {
    EMPTY_GOAL_VALUE(HttpStatus.BAD_REQUEST, "GOAL-400-01", "러닝 목표 값이 비어있습니다.");

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
