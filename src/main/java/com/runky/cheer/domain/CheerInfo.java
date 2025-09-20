package com.runky.cheer.domain;

public final class CheerInfo {
	private CheerInfo() {
	}

	public record Detail(Long cheerId, Long runningId, Long senderId, Long receiverId, java.time.Instant sentAt) {
	}
}
