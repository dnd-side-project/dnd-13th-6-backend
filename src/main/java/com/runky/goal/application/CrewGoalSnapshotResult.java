package com.runky.goal.application;

import com.runky.goal.domain.CrewGoalSnapshot;
import java.math.BigDecimal;

public record CrewGoalSnapshotResult(
        Long id,
        Long crewId,
        BigDecimal goal,
        boolean achieved
) {
    public static CrewGoalSnapshotResult from(CrewGoalSnapshot snapshot) {
        return new CrewGoalSnapshotResult(
                snapshot.getId(),
                snapshot.getCrewId(),
                snapshot.getGoal().value(),
                snapshot.getAchieved()
        );
    }
}
