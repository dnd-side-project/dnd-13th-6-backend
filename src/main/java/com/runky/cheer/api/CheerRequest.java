package com.runky.cheer.api;

public final class CheerRequest {
	private CheerRequest() {
	}

	public record Send(Long receiverId, String message) {
	}

}
