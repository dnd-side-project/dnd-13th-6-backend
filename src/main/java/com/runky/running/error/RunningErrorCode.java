package com.runky.running.error;

import org.springframework.http.HttpStatus;

import com.runky.global.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RunningErrorCode implements ErrorCode {
	/* R1xx: Running 상태/조회 */
	NOT_FOUND_RUNNING(HttpStatus.NOT_FOUND, "R101", "런닝을 찾을 수 없습니다."),
	ALREADY_ACTIVE_RUNNING(HttpStatus.CONFLICT, "R102", "이미 진행 중인 런닝이 있습니다."),
	NOT_ACTIVE_RUNNING(HttpStatus.CONFLICT, "R103", "시작 상태가 아니므로 종료할 수 없습니다."),

	/* R2xx: Running Track 저장/포맷 */
	TRACK_ALREADY_EXISTS(HttpStatus.CONFLICT, "R201", "이미 트랙이 저장되어 있습니다."),
	INVALID_TRACK_FORMAT(HttpStatus.BAD_REQUEST, "R202", "지원하지 않는 트랙 포맷입니다."),
	EMPTY_TRACK_POINTS(HttpStatus.BAD_REQUEST, "R203", "트랙 좌표가 비어있습니다."),
	EXCESSIVE_TRACK_POINTS(HttpStatus.UNPROCESSABLE_ENTITY, "R204", "트랙 좌표 개수가 허용 범위를 초과했습니다."),

	/* R3xx: 권한/입력 검증 */
	FORBIDDEN_RUNNING_ACCESS(HttpStatus.FORBIDDEN, "R301", "해당 런닝에 접근 권한이 없습니다."),
	INVALID_END_METRICS(HttpStatus.BAD_REQUEST, "R302", "종료 메트릭 값이 올바르지 않습니다."),

	/* R9xx: 인프라/제약 위반/기타 */
	UNIQUE_ACTIVE_CONSTRAINT_VIOLATED(HttpStatus.CONFLICT, "R901", "활성 런닝 중복 제약에 위배되었습니다."),
	EVENT_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R902", "이벤트 발행에 실패했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
