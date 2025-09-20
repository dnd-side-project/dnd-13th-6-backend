package com.runky.reward.application;

public final class RewardCriteria {
	private RewardCriteria() {
	}

	public record Find(Long userId) {
	}

	public record Gotcha(Long userId) {
	}
}
