package com.runky.running.api;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import com.runky.global.security.auth.MemberPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RunningLocationWsController implements RunningLocationWsApiSpec {

	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/runnings/{runningId}/location")
	public void publish(
		@AuthenticationPrincipal MemberPrincipal requester,
		@DestinationVariable Long runningId,
		@Payload LocationMessage payload
	) {
		Long memberId = requester != null ? requester.memberId() : null;

		log.info("[WS][@MessageMapping][IN] runningId={}, memberId={}, payload={}", runningId, memberId, payload);

		RoomEvent event = new RoomEvent("LOCATION", memberId, payload.x(), payload.y(), payload.timestamp());
		String dest = "/topic/runnings/" + runningId;

		messagingTemplate.convertAndSend(dest, event);

		log.info("[WS][@MessageMapping][OUT] dest={}, event={}", dest, event);
	}

	public record LocationMessage(double x, double y, long timestamp) {
	}

	public record RoomEvent(String type, Long runnerId, Double x, Double y, long timestamp) {
	}
}
