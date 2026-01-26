package com.runky.running.interfaces.websocket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * WebSocket 응원 메시지 요청
 */
public record CheerMessage(
	@NotNull(message = "러닝 ID는 필수입니다")
	Long runningId,

	@NotNull(message = "수신자 ID는 필수입니다")
	Long receiverId,

	@NotBlank(message = "응원 메시지는 필수입니다")
	@Size(min = 1, max = 200, message = "응원 메시지는 1~200자 이내여야 합니다")
	String message
) {
}
