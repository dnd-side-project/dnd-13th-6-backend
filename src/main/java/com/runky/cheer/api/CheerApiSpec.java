package com.runky.cheer.api;

import com.runky.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cheer API", description = "응원 메시지 전송 API")
public interface CheerApiSpec {

	@Operation(
		summary = "응원 메시지 전송",
		description = """
			특정 달리기(running)에 참여 중인 다른 사용자에게 응원 메시지를 전송합니다.
			"""
	)
	@Parameter(
		name = "X-USER-ID",
		description = "응원을 보내는 사용자 ID",
		required = true,
		in = ParameterIn.HEADER,
		schema = @Schema(type = "integer", format = "int64", example = "100")
	)
	@Parameter(
		name = "runningId",
		description = "응원할 달리기의 ID",
		required = true,
		in = ParameterIn.PATH,
		schema = @Schema(type = "integer", format = "int64", example = "1")
	)
	ApiResponse<CheerResponse.Sent> send(
		Long senderId,
		Long runningId,
		@Schema(description = "응원 메시지 전송 요청 바디") CheerRequest.Send request
	);
}
