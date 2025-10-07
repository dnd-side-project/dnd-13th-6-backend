package com.runky.goal.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public final class GoalCommand {
	private GoalCommand() {
	}

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

	public record CrewSnapshots(
		Set<Long> crewIds,
		LocalDate localDate
	) {
	}

	public record Init(
		Long memberId
	) {
	}

    public record UpdateDistance(
            Long memberId,
            Set<Long> crewIds,
            BigDecimal distance,
            LocalDate date
    ) {
    }
}
