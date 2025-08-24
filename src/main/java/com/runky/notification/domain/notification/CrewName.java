package com.runky.notification.domain.notification;

public record CrewName(String value) {

	@Override
	public String toString() {
		return value;
	}
}
