package com.runky.running.interfaces.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.runky.cheer.application.CheerCriteria;
import com.runky.cheer.application.CheerFacade;
import com.runky.cheer.application.CheerResult;
import com.runky.global.security.auth.MemberPrincipal;
import com.runky.running.infra.websocket.session.WebSocketSessionManager;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 러닝 응원 메시지 WebSocket 컨트롤러
 * - 크루원이 러너에게 1:1 응원 메시지 전송
 * - 기존 CheerFacade를 사용하여 DB 저장 및 알림 전송
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class RunningCheerWsController {

	private final WebSocketSessionManager sessionManager;
	private final CheerFacade cheerFacade;

	/**
	 * 응원 메시지 전송
	 * /app/cheer로 발행하면 receiverId에게 /user/{userId}/queue/cheer로 전달
	 */
	@MessageMapping("/cheer")
	public void sendCheer(
		@Valid @Payload CheerMessage payload,
		SimpMessageHeaderAccessor accessor
	) {
		MemberPrincipal principal = (MemberPrincipal)accessor.getSessionAttributes().get("Principal");
		Long senderId = principal.memberId();

		// CheerFacade를 통해 응원 메시지 저장 및 알림 전송
		CheerResult.Sent result = cheerFacade.send(
			new CheerCriteria.Send(
				payload.runningId(),
				senderId,
				payload.receiverId(),
				payload.message()
			)
		);

		// WebSocket을 통한 실시간 응원 메시지 전송
		CheerEvent event = CheerEvent.of(
			senderId,
			result.receiverId().toString(), // nickname은 CheerFacade에서 이미 처리됨
			payload.message()
		);
		sessionManager.sendToUser(payload.receiverId(), "/queue/cheer", event);

		log.info("[Cheer WS] 응원 메시지 전송 완료 - cheerId={}, from={}, to={}, runningId={}",
			result.cheerId(), senderId, payload.receiverId(), payload.runningId());
	}
}
