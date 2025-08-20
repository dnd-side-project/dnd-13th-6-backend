package com.runky.goal.api;

import java.math.BigDecimal;

public class GoalRequest {

    public record Goal(
            BigDecimal goal
    ) {
    }

    private GoalRequest() {
    }
}
