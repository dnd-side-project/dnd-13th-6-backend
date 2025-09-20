package com.runky.notification.api;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.runky.notification.application.NotificationResult;

public final class NotificationResponse {
	private NotificationResponse() {
	}

	public record Message(String type, String raw, Map<String, String> variables
	) {
	}

	public record Item(
		Long id, String title, String text,
		Long senderId, boolean read, String createdAt,
		Message message
	) {
		public static Item from(NotificationResult.Summary s) {
			return new Item(
				s.id(), s.title(), s.text(),
				s.senderId(), s.read(), DateTimeFormatter.ISO_INSTANT.format(s.createdAt()),
				new Message(s.message().type(), s.message().raw(), s.message().variables())
			);
		}
	}

	public record Items(List<Item> values) {
		public static Items from(NotificationResult.Items r) {
			return new Items(r.values().stream().map(Item::from).toList());
		}
	}
}
