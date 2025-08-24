package com.runky.cheer.application;

public sealed interface CheerCriteria {
	record Send(Long runningId, Long senderId, Long receiverId, String message) implements CheerCriteria {
	}

}
