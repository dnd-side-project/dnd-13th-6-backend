package com.runky.cheer.api;

public sealed interface CheerRequest {
	record Send(Long receiverId, String message) implements CheerRequest {
	}

}
