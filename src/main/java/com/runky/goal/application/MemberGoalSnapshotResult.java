package com.runky.goal.application;

import com.runky.goal.domain.MemberGoalSnapshot;
import java.math.BigDecimal;

public record MemberGoalSnapshotResult(
        Long id,
        Long memberId,
        BigDecimal goal
) {
    public static MemberGoalSnapshotResult from(MemberGoalSnapshot snapshot) {
        return new MemberGoalSnapshotResult(
                snapshot.getId(),
                snapshot.getMemberId(),
                snapshot.getGoal().value()
        );
    }
}
