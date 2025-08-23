package com.runky.notification.api;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.runky.notification.application.NotificationResult;

public sealed interface NotificationResponse {
	record Item(
		Long id,
		String title,
		String message,
		Long senderId,
		boolean read,
		String createdAt
	) implements NotificationResponse {
		public static Item from(NotificationResult.Summary s) {
			return new Item(
				s.id(), s.title(), s.message(),
				s.senderId(), s.read(),
				DateTimeFormatter.ISO_INSTANT.format(s.createdAt())
			);
		}
	}

	record Items(List<Item> values) implements NotificationResponse {
		public static Items from(NotificationResult.Items r) {
			return new Items(r.values().stream().map(Item::from).toList());
		}
	}
}
