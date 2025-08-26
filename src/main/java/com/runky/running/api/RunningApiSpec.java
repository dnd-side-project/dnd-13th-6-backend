package com.runky.running.api;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Running API", description = "Runky Running API입니다.")
public interface RunningApiSpec {

	@Operation(summary = "런닝 시작", description = "런닝 세션을 시작하고, 실시간 위치를 전송할 WebSocket 주소를 반환합니다.")
	ApiResponse<RunningResponse.Start> start(
		@Parameter(hidden = true) MemberPrincipal requester
	);

	@Operation(summary = "런닝 종료", description = "런닝을 종료하고 전체 기록을 저장합니다.")
	ApiResponse<RunningResponse.End> end(
		@Parameter(hidden = true) MemberPrincipal requester,
		@Schema(description = "종료할 런닝 ID") Long runningId,
		@Schema(description = "런닝 요약 및 트랙 정보") RunningRequest.End request
	);

	@Operation(
		summary = "오늘 달린 기록 요약 조회",
		description = "요청자 기준 KST(UTC+9) ‘오늘’에 **완료된** 모든 런닝 기록을 합산하여 요약을 반환합니다."
	)
	ApiResponse<RunningResponse.TodaySummary> getToday(
		@Parameter(hidden = true) MemberPrincipal requester
	);
}

