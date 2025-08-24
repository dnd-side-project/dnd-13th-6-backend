package com.runky.notification.api;

import com.runky.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notification API", description = "알림 조회 API")
public interface NotificationApiSpec {

	@Operation(
		summary = "최신 알림 10개 조회",
		description = "사용자의 최신 알림 10개를 조회합니다."
	)
	@Parameter(
		name = "X-USER-ID",
		description = "사용자 ID",
		required = true,
		in = ParameterIn.HEADER,
		schema = @Schema(type = "integer", format = "int64", example = "123")
	)
	ApiResponse<NotificationResponse.Items> getRecentTop10(
		Long receiverId
	);
}
