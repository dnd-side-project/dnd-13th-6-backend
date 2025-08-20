package com.runky.running.api;

import com.runky.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Running API", description = "Runky Running API입니다.")
public interface RunningApiSpec {

	@Operation(summary = "런닝 시작", description = "런닝 세션을 시작하고, 실시간 위치를 전송할 WebSocket 주소를 반환합니다.")
	@Parameter(name = "X-USER-ID", description = "사용자 ID", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "integer", format = "int64"))
	ApiResponse<RunningResponse.Start> start(
		Long userId
	);

	@Operation(summary = "런닝 종료", description = "런닝을 종료하고 전체 기록을 저장합니다.")
	@Parameter(name = "X-USER-ID", description = "사용자 ID", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "integer", format = "int64"))
	ApiResponse<RunningResponse.End> end(
		Long userId,
		@Schema(description = "종료할 런닝 ID") Long runningId,
		@Schema(description = "런닝 요약 및 트랙 정보") RunningRequest.End request
	);
}

