package com.runky.running.interfaces.websocket;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.runky.global.security.auth.MemberPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RunningLocationWsController implements RunningLocationWsApiSpec {

	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/runnings/{runningId}/location")
	public void publish(
		@DestinationVariable Long runningId,
		@Valid @Payload LocationMessage payload,
		SimpMessageHeaderAccessor accessor
	) {
		MemberPrincipal principal = (MemberPrincipal)accessor.getSessionAttributes().get("Principal");
		Long runnerId = principal.memberId();

		RoomEvent event = new RoomEvent("LOCATION", runningId, runnerId, payload.x(), payload.y(), payload.timestamp());
		String dest = "/topic/runnings/" + runningId;
		messagingTemplate.convertAndSend(dest, event);
	}

}
