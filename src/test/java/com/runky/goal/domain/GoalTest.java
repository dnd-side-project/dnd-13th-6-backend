package com.runky.goal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.runky.global.error.GlobalException;
import com.runky.goal.error.GoalErrorCode;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GoalTest {

    @Test
    @DisplayName("목표 생성 시, null 값이 들어오면, EMPTY_GOAL_VALUE 예외가 발생한다.")
    void throwEmptyGoalValueException_whenValueIsNull() {
        Exception exception = assertThrows(Exception.class, () -> new Goal(null));

        assertThat(exception)
                .usingRecursiveComparison()
                .isEqualTo(new GlobalException(GoalErrorCode.EMPTY_GOAL_VALUE));
    }

    @Test
    @DisplayName("목표 생성 시, 소수점 둘째 자리까지만 저장된다.")
    void saveOnlyTwoDecimalPlaces_whenCreatingGoal() {
        Goal goal = new Goal(new BigDecimal("100.129"));

        assertThat(goal.value()).isEqualTo("100.12");
    }
}