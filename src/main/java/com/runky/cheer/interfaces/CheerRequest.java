package com.runky.cheer.interfaces;

public final class CheerRequest {
	private CheerRequest() {
	}

	public record Send(Long receiverId, String message) {
	}

}
