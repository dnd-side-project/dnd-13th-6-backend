package com.runky.notification.error;

import org.springframework.http.HttpStatus;

import com.runky.global.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

	/* NDT1xx: DeviceToken 상태/조회 */
	NOT_FOUND_DEVICE_TOKEN(HttpStatus.NOT_FOUND, "NDT101", "디바이스 토큰을 찾을 수 없습니다."),
	NOT_EXIST_TO_DELETE_DEVICE_TOKEN(HttpStatus.NOT_FOUND, "NDT102", "삭제할 디바이스 토큰이 존재하지 않습니다."),
	ALREADY_REGISTERED_DEVICE_TOKEN(HttpStatus.CONFLICT, "NDT103", "이미 존재하는 디바이스 토큰 입니다."),
	EMTPY_TOKEN_OWNER_IDS(HttpStatus.INTERNAL_SERVER_ERROR, "NDT104", "디바이스 토큰을 조회할 멤버 아이디들이 비어있습니다"),
	/* NDT2xx: DeviceToken  저장/포맷 */
	DUPLICATE_UNIQUE_KEY_DEVICE_TOKEN(HttpStatus.CONFLICT, "NDT201", "같은 디바이스 토큰을 중복 저장할 수 없습니다."),

	/* NDT3xx: 권한/입력 검증 */

	/* NDT9xx: 인프라/제약 위반/기타 */

	/** notification Message**/
	/*NM2xx: 저장/포맷 */
	EMPTY_VAR_KEY_NOTIFICATION_MESSAGE(HttpStatus.INTERNAL_SERVER_ERROR, "NM101", "알림 메세지 상수를 채워주세요.");
	private final HttpStatus status;
	private final String code;
	private final String message;
}
