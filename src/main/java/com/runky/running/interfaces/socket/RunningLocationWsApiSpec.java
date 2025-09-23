package com.runky.running.api.socket;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 실시간 런닝 위치 공유 WebSocket API 명세
 */
@Tag(name = "Running Location (WebSocket)", description = "실시간 런닝 위치 공유 WebSocket API")
public interface RunningLocationWsApiSpec {

	@Operation(summary = "런닝 중 위치 정보 발행 (메시지 전송)",
		description = "사용자가 런닝 중 자신의 위치 정보를 서버로 전송합니다.\n\n" +
			"이 API는 WebSocket을 통해 통신합니다.\n\n" +
			"**메시지 발행(PUBLISH) 주소:** `/pub/runnings/{runningId}/location"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "메시지가 성공적으로 토픽에 발행됨",
			content = @Content(mediaType = "application/json",
				schema = @Schema(implementation = RunningLocationWsController.RoomEvent.class)))
	})
	void publish(

		@Parameter(name = "runningId", description = "참여 중인 런닝방 ID",
			in = ParameterIn.PATH, required = true, example = "1")
		Long runningId,
		RunningLocationWsController.LocationMessage payload,
		SimpMessageHeaderAccessor accessor
	);

}
