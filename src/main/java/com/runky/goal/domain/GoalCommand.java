package com.runky.goal.domain;

import java.time.LocalDate;
import java.util.Set;

public class GoalCommand {

    public record Snapshot(LocalDate date) {
    }

    public record CrewSnapshot(
            Long crewId,
            Set<Long> memberIds,
            LocalDate localDate
    ) {
    }
}
