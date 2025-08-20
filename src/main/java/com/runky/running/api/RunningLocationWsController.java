package com.runky.running.api;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class RunningLocationWsController implements RunningLocationWsApiSpec {

	@MessageMapping("/runnings/{runningId}/location")
	@SendTo("/topic/runnings/{runningId}")
	public RoomEvent publish(@DestinationVariable Long runningId, @Payload LocationMessage payload) {
		return new RoomEvent("LOCATION", payload.runnerId(), payload.x(), payload.y(), payload.timestamp());
	}

	public record LocationMessage(Long runnerId, double x, double y, long timestamp) {
	}

	public record RoomEvent(String type, Long runnerId, Double x, Double y, long timestamp) {
	}
}
