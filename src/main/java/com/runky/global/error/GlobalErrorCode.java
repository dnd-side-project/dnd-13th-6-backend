package com.runky.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
    OTHER(HttpStatus.INTERNAL_SERVER_ERROR, "G100", "서버에 오류가 발생했습니다"),
    JSON_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G101", "JSON 파싱에 실패했습니다"),

    // 전체
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "GLOBAL_200", "허용되지 않은 메서드입니다"),
    VALID_EXCEPTION(HttpStatus.BAD_REQUEST, "GLOBAL_300", "유효 하지 않은 요청입니다."),
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "GLOBAL_400", "허용되지 않은 사용자입니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "GLOBAL_500", "토큰이 만료되었습니다."),
    USER_CONFLICT(HttpStatus.CONFLICT, "GLOBAL_600", "이미 가입된 내역이 있습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-404", "요청하신 자원을 찾을 수 없습니다."),
    NOT_LOGIN_MEMBER(HttpStatus.UNAUTHORIZED, "GLOBAL-401-1", "로그인 후 이용 가능한 서비스입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "GLOBAL_401-1", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "GLOBAL_401-2", "유효하지 않은 토큰입니다.");
    private final HttpStatus status;
    private final String code;
    private final String message;
}
