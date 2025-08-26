package com.runky.notification.api;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Device Token API", description = "FCM 디바이스 토큰 등록/삭제 API")
public interface DeviceTokenApiSpec {

	@Operation(
		summary = "디바이스 토큰 등록",
		description = """
			사용자의 FCM 디바이스 토큰을 등록합니다.
			- 이미 등록된 토큰(유니크 키 충돌)일 경우 409 응답으로 처리됩니다.
			"""
	)
	ApiResponse<Void> register(
		@Parameter(hidden = true) MemberPrincipal requester,
		@Schema(description = "디바이스 토큰 등록 요청 바디") DeviceTokenRequest.Register request
	);

	@Operation(
		summary = "디바이스 토큰 삭제",
		description = """
			사용자의 FCM 디바이스 토큰을 삭제합니다.
			- 요청된 토큰이 존재하지 않으면 404 또는 사내 규약의 에러 코드로 응답됩니다.
			"""
	)
	ApiResponse<DeviceTokenResponse.Delete> delete(
		@Parameter(hidden = true) MemberPrincipal requester,
		@Schema(description = "디바이스 토큰 삭제 요청 바디") DeviceTokenRequest.Delete request
	);
}
