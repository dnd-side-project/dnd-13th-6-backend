package com.runky.cheer.application;

public final class CheerCriteria {
	private CheerCriteria() {
	}

	public record Send(Long runningId, Long senderId, Long receiverId, String message) {
	}

}
