package com.runky.goal.api;

import java.math.BigDecimal;

public class GoalResponse {

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

    private GoalResponse() {
    }
}
