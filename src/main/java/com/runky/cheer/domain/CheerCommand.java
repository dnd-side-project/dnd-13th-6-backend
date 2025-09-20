package com.runky.cheer.domain;

public final class CheerCommand {
	private CheerCommand() {
	}

	public record Create(Long runningId, Long senderId, Long receiverId, String message) {
	}
}
