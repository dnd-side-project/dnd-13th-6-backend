package com.runky.cheer.application;

import java.time.Instant;

public final class CheerResult {
	private CheerResult() {
	}

	public record Sent(Long cheerId, Long runningId, Long senderId, Long receiverId, Instant sentAt) {
	}

}
