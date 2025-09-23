package com.runky.goal.interfaces;

import java.math.BigDecimal;

public final class GoalResponse {

	private GoalResponse() {
	}

	public record Goal(
		BigDecimal goal
	) {
	}

	public record Achieve(
		boolean achieved
	) {
	}

	public record Clover(
		Long count
	) {
	}
}
