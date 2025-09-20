package com.runky.cheer.api;

import java.time.Instant;

import com.runky.cheer.application.CheerResult;

public final class CheerResponse {
	private CheerResponse() {
	}

	public record Sent(Long cheerId, Long runningId, Long senderId, Long receiverId, Instant sentAt) {
		public static Sent from(CheerResult.Sent result) {
			return new Sent(result.cheerId(), result.runningId(), result.senderId(), result.receiverId(),
				result.sentAt());
		}
	}
}
