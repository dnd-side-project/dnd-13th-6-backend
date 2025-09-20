package com.runky.reward.api;

import java.util.List;

public final class RewardResponse {

	private RewardResponse() {
	}

	public record Images(
		List<Image> badges
	) {
	}

	public record Image(
		Long badgeId,
		String badge
	) {
	}

	public record Gotcha(
		Long id,
		String imageUrl,
		String name
	) {
	}

	public record Clover(
		Long count
	) {
	}
}
