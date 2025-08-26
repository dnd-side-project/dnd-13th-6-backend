package com.runky.notification.api;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notification API", description = "알림 조회 API")
public interface NotificationApiSpec {

	@Operation(
		summary = "최신 알림 10개 조회",
		description = "사용자의 최신 알림 10개를 조회합니다."
	)
	ApiResponse<NotificationResponse.Items> getRecentTop10(
		@Parameter(hidden = true) MemberPrincipal requester
	);
}
