package com.runky.cheer.application;

import java.time.Instant;

public sealed interface CheerResult {

	record Sent(Long cheerId, Long runningId, Long senderId, Long receiverId, String message, Instant sentAt)
		implements CheerResult {
	}

}
