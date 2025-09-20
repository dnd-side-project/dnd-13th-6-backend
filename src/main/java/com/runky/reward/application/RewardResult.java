package com.runky.reward.application;

public final class RewardResult {
	private RewardResult() {
	}

	public record Badge(
		Long badgeId,
		String ImageUrl,
		String name
	) {
	}

	public record Clover(
		Long userId,
		Long count
	) {
	}

	public record Gotcha(
		Long id,
		String name,
		String imageUrl
	) {
	}
}
