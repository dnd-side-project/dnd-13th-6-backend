package com.runky.goal.application;

import com.runky.goal.domain.MemberGoalSnapshot;
import java.math.BigDecimal;

public record MemberGoalSnapshotResult(
        Long id,
        Long memberId,
        BigDecimal goal,
        BigDecimal distance,
        Boolean achieved
) {
    public static MemberGoalSnapshotResult from(MemberGoalSnapshot snapshot) {
        return new MemberGoalSnapshotResult(
                snapshot.getId(),
                snapshot.getMemberId(),
                snapshot.getGoal().value(),
                snapshot.getRunDistance(),
                snapshot.getAchieved()
        );
    }

    public record Clover(
            Long count
    ) {
    }
}
