package com.runky.notification.interfaces.consumer;

import java.util.List;

import com.runky.notification.domain.notification.NotificationMessage;

import jakarta.validation.constraints.NotNull;

public final class NotificationEvent {
	private NotificationEvent() {
	}

	public record NotifyToOne(@NotNull Long senderId, @NotNull Long receiverId, @NotNull NotificationMessage args) {
	}

	public record NotifyToMany(@NotNull Long senderId, @NotNull List<Long> receiverIds,
							   @NotNull NotificationMessage args) {
	}

}
