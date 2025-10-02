package com.runky.goal.application;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class GoalCriteria {
	private GoalCriteria() {
	}

	public record MemberGoal(Long memberId) {
	}

	public record CrewGoal(Long crewId) {
	}

	public record Update(Long memberId, BigDecimal goal) {
	}

	public record LastWeekClover(Long memberId) {
	}

    public record UpdateDistance(Long memberId, BigDecimal distance, LocalDate date) {
    }
}
