package com.runky.goal.domain;

import com.runky.global.error.GlobalException;
import com.runky.goal.error.GoalErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode
public class Goal {

    @Column(name = "goal", nullable = false, scale = 2)
    private BigDecimal value;

    protected Goal() {
    }

    public Goal(BigDecimal value) {
        if (value == null) {
            throw new GlobalException(GoalErrorCode.EMPTY_GOAL_VALUE);
        }
        this.value = value.setScale(2, RoundingMode.DOWN);
    }

    public BigDecimal value() {
        return value;
    }

    public boolean isLessThanOrEqualTo(BigDecimal other) {
        return this.value.compareTo(other) <= 0;
    }
}
