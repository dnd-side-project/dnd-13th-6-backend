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
	ALREADY_ENDED_RUNNING(HttpStatus.CONFLICT, "R104", "이미 종료된 런닝입니다."),

	/* R2xx: Running Track 저장/포맷 */
	NOT_FOUND_RUNNING_TRACK(HttpStatus.NOT_FOUND, "R205", "해당 런닝의 런닝 트랙이 저장되지 않았습니다."),
	TRACK_ALREADY_EXISTS(HttpStatus.CONFLICT, "R201", "이미 트랙이 저장되어 있습니다."),
	INVALID_TRACK_FORMAT(HttpStatus.BAD_REQUEST, "R202", "지원하지 않는 트랙 포맷입니다."),
	EMPTY_TRACK_POINTS(HttpStatus.BAD_REQUEST, "R203", "트랙 좌표가 비어있습니다."),
	EXCESSIVE_TRACK_POINTS(HttpStatus.UNPROCESSABLE_ENTITY, "R204", "트랙 좌표 개수가 허용 범위를 초과했습니다."),

	/* R3xx: 권한/입력 검증 */
	FORBIDDEN_RUNNING_ACCESS(HttpStatus.FORBIDDEN, "R301", "해당 런닝에 접근 권한이 없습니다."),
	INVALID_END_METRICS(HttpStatus.BAD_REQUEST, "R302", "종료 메트릭 값이 올바르지 않습니다."),

	// === WS 전용/공통 ===
	INVALID_LOCATION_VALUE(HttpStatus.BAD_REQUEST, "R303", "위치 좌표 또는 입력 값이 올바르지 않습니다."),
	PAYLOAD_INVALID(HttpStatus.BAD_REQUEST, "R304", "메시지 포맷이 올바르지 않습니다."),
	UNAUTHORIZED_SESSION(HttpStatus.UNAUTHORIZED, "R305", "세션 인증 정보가 없습니다."),
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "R300", "입력 값이 올바르지 않습니다."),
	FORBIDDEN_WS_ACCESS(HttpStatus.FORBIDDEN, "R306", "해당 요청에 대한 권한이 없습니다."),
	HEADER_MISSING(HttpStatus.BAD_REQUEST, "R307", "필수 헤더가 누락되었습니다."),
	TYPE_CONVERSION_FAILED(HttpStatus.BAD_REQUEST, "R308", "요청 값의 타입 변환에 실패했습니다."),
	CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "R310", "입력 제약 조건을 위반했습니다."),
	OUTBOUND_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "R311", "응답 직렬화 중 오류가 발생했습니다."),
	MESSAGE_HANDLING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "R312", "메시지 처리 중 오류가 발생했습니다."),
	MESSAGING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "R313", "메시징 처리 중 알 수 없는 오류가 발생했습니다."),
	BUSINESS_INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "R321", "잘못된 요청 값입니다."),
	BUSINESS_ILLEGAL_STATE(HttpStatus.CONFLICT, "R322", "현재 상태에서 수행할 수 없는 작업입니다."),

	/* R9xx: 인프라/제약 위반/기타 */
	UNIQUE_ACTIVE_CONSTRAINT_VIOLATED(HttpStatus.CONFLICT, "R901", "활성 런닝 중복 제약에 위배되었습니다."),
	EVENT_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R902", "이벤트 발행에 실패했습니다."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "R399", "알 수 없는 오류가 발생했습니다."),
	;

	private final HttpStatus status;
	private final String code;
	private final String message;
}
