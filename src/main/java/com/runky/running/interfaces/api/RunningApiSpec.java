package com.runky.running.interfaces.api;

import com.runky.global.response.ApiResponse;
import com.runky.global.security.auth.MemberPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
		@Schema(description = "런닝 요약 및 트랙 정보") com.runky.running.interfaces.api.RunningRequest.End request
	);

	@Operation(
		summary = "오늘 달린 기록 요약 조회",
		description = "요청자 기준 KST(UTC+9) ‘오늘’에 **완료된** 모든 런닝 기록을 합산하여 요약을 반환합니다."
	)
	ApiResponse<RunningResponse.TodaySummary> getToday(
		@Parameter(hidden = true) MemberPrincipal requester
	);

	@Operation(
		summary = "내 주간 누적 거리 조회",
		description = "요청자 기준 KST(UTC+9) 이번 주(월~일)의 **완료된** 모든 런닝 기록 총합 거리를 반환합니다."
	)
	ApiResponse<RunningResponse.MyWeeklyTotalDistance> getMyWeeklyTotalDistance(
		@Parameter(hidden = true) MemberPrincipal requester
	);

	@Operation(
		summary = "런닝 결과 단건 조회",
		description = "특정 런닝 ID에 대한 최종 기록(일반적으로 종료된 세션 기준)을 조회합니다."
	)
	ApiResponse<RunningResponse.RunResult> getRunResult(
		@Parameter(hidden = true) MemberPrincipal requester,
		@Parameter(
			name = "runningId",
			in = ParameterIn.PATH,
			required = true,
			description = "조회할 런닝 ID",
			schema = @Schema(type = "integer", format = "int64", example = "123")
		)
		Long runningId
	);

	@Operation(
		summary = "런닝 결과 단건 조회",
		description = "특정 런닝 ID에 대한 최종 기록(일반적으로 종료된 세션 기준)을 조회합니다."
	)
	ApiResponse<RunningResponse.RemovedRunning> removeActiveRunning(
		@Parameter(hidden = true) MemberPrincipal requester,
		@Schema(description = "삭제할 런닝 ID") Long runningId
	);
}

