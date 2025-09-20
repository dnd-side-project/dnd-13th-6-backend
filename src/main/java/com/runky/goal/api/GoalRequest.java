package com.runky.goal.api;

import java.math.BigDecimal;

public final class GoalRequest {

	private GoalRequest() {
	}

	public record Goal(
		BigDecimal goal
	) {
	}
}
