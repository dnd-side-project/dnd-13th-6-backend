package com.runky.goal.application;

import com.runky.goal.domain.MemberGoal;
import java.math.BigDecimal;

public record MemberGoalResult(
        Long id,
        Long memberId,
        BigDecimal goal
) {
    public static MemberGoalResult from(MemberGoal memberGoal) {
        return new MemberGoalResult(
                memberGoal.getId(),
                memberGoal.getMemberId(),
                memberGoal.getGoal().value()
        );
    }
}
