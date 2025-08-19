package com.runky.goal.api;

import java.math.BigDecimal;

public class GoalResponse {

    public record Goal(
            BigDecimal goal
    ) {
    }

    private GoalResponse() {
    }
}
