package com.runky.cheer.domain;

public interface CheerInfo {
	record Detail(Long cheerId, Long runningId, Long senderId, Long receiverId, java.time.Instant sentAt) {
	}
}
