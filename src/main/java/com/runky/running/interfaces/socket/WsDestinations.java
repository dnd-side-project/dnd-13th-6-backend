package com.runky.running.api.socket;

public final class WsDestinations {
	private WsDestinations() {
	}

	public static String publish(long runningId) {
		return "/app/runnings/%d/location".formatted(runningId);
	}

	public static String subscribe(long runningId) {
		return "/topic/runnings/%d".formatted(runningId);
	}
}
