package com.runky.goal.domain.batch;

import java.math.BigDecimal;

public record CrewGoalSum(
        Long crewId,
        BigDecimal totalGoal
) {
}
