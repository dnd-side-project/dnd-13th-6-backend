package com.runky.cheer.api;

import java.time.Instant;

import com.runky.cheer.application.CheerResult;

public interface CheerResponse {
	record Sent(Long cheerId, Long runningId, Long senderId, Long receiverId, String message,
				Instant sentAt)
		implements CheerResponse {
		public static Sent from(CheerResult.Sent result) {
			return new Sent(result.cheerId(), result.runningId(), result.senderId(), result.receiverId(),
				result.message(),
				result.sentAt());
		}
	}
}
