package com.runky.cheer.domain;

public sealed interface CheerCommand {
	record Create(Long runningId, Long senderId, Long receiverId, String message) implements CheerCommand {
	}
}
