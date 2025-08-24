package com.runky.notification.domain.notification;

public record Nickname(String value) {

	@Override
	public String toString() {
		return value;
	}
}
