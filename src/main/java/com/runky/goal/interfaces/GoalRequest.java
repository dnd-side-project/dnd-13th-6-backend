package com.runky.goal.interfaces;

import java.math.BigDecimal;

public final class GoalRequest {

	private GoalRequest() {
	}

	public record Goal(
		BigDecimal goal
	) {
	}
}
