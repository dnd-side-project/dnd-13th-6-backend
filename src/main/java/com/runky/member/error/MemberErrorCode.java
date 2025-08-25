package com.runky.member.error;

import com.runky.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-404-01", "회원이 존재하지 않습니다."),

    INVALID_EXTERNAL_ACCOUNT(HttpStatus.BAD_REQUEST, "MEMBER-400-01", "외부 계정 정보가 올바르지 않습니다."),
    BLANK_NICKNAME(HttpStatus.BAD_REQUEST, "MEMBER-400-02", "닉네임은 공백일 수 없습니다."),
    OVER_LENGTH_NICKNAME(HttpStatus.BAD_REQUEST, "MEMBER-400-03", "닉네임은 10자 이내이어야 합니다."),
    INVALID_FORMAT_NICKNAME(HttpStatus.BAD_REQUEST, "MEMBER-400-04", "닉네임은 한글, 영어 대소문자, 숫자만 포함할 수 있습니다."),

    DUPLICATE_MEMBER(HttpStatus.CONFLICT, "MEMBER-409-01", "이미 가입된 회원입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "MEMBER-409-02", "이미 사용중인 닉네임입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
