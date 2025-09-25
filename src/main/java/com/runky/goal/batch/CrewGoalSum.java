package com.runky.goal.batch;

import java.math.BigDecimal;

public record CrewGoalSum(
        Long crewId,
        BigDecimal totalGoal
) {
}
