package com.runky.crew.error;

import com.runky.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CrewErrorCode implements ErrorCode {
    OVER_CREW_COUNT(HttpStatus.CONFLICT, "C100", "크루는 최대 5개까지 참여할 수 있습니다"),
    OVER_CREW_NAME(HttpStatus.BAD_REQUEST, "C101", "크루 이름은 최대 15자까지 가능합니다."),
    BLANK_CREW_NAME(HttpStatus.BAD_REQUEST, "C102", "크루 이름은 공백일 수 없습니다."),
    OVER_CODE_LENGTH(HttpStatus.BAD_REQUEST, "C103", "코드의 길이는 6자여야 합니다."),
    INVALID_CODE_PATTERN(HttpStatus.BAD_REQUEST, "C104", "코드는 영문 대소문자와 숫자만 포함할 수 있습니다."),
    OVER_CREW_MEMBER_COUNT(HttpStatus.CONFLICT, "C105", "크루는 최대 6명까지 참여할 수 있습니다."),
    NOT_FOUND_CREW(HttpStatus.NOT_FOUND, "C106", "크루를 찾을 수 없습니다."),
    BANNED_MEMBER(HttpStatus.FORBIDDEN, "C107", "추방된 멤버입니다."),
    ALREADY_IN_CREW(HttpStatus.CONFLICT, "C108", "이미 크루에 참여한 상태입니다."),
    NOT_CREW_MEMBER(HttpStatus.FORBIDDEN, "C109", "크루 멤버가 아닙니다."),
    NOT_CREW_LEADER(HttpStatus.FORBIDDEN, "C110", "크루 리더가 아닙니다."),
    HAVE_TO_DELEGATE_LEADER(HttpStatus.BAD_REQUEST, "C111", "리더는 탈퇴 전 다른 멤버에게 리더를 위임해야 합니다."),
    NOT_IN_CREW(HttpStatus.BAD_REQUEST, "C112", "현재 참여중인 크루가 없습니다."),
    LAST_CREW_MEMBER(HttpStatus.BAD_REQUEST, "C113", "크루에 마지막으로 남은 사용자는 탈퇴 대신 해체해야 합니다."),
    INVALID_NOTICE(HttpStatus.BAD_REQUEST, "C114", "크루 공지사항은 1자 이상, 20자 이하여야 합니다."),
	NOT_ENOUGH_CREW_MEMBER(HttpStatus.CONFLICT, "C115", "나를 제외한 크루원이 존재해야 리더를 위임할 수 있습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
