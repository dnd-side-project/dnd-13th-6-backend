package com.runky.goal.domain;

import java.math.BigDecimal;
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

    public record GetMemberSnapshot(
            Long memberId,
            LocalDate localDate
    ) {
    }

    public record GetCrewSnapshot(
            Long crewId,
            LocalDate localDate
    ) {
    }

    public record Update(
            Long memberId,
            BigDecimal goal
    ) {
    }
}
