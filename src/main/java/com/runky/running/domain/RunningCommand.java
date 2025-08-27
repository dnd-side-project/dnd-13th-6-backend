package com.runky.running.domain;

import java.time.LocalDateTime;

public sealed interface RunningCommand {
	record Start(
		Long runnerId
	) implements RunningCommand {
	}

	record End(
		Long runningId,
		Long runnerId,
		Double totalDistanceMinutes,
		Long durationSeconds,
		Double avgSpeedMPS,
		String format,
		String points,
		int pointCount
	) implements RunningCommand {
	}

	record WeekDistance(
		Long runnerId,
		LocalDateTime from,
		LocalDateTime to
	) implements RunningCommand {
	}

	record MyWeeklyTotalDistance(
		Long runnerId
	) implements RunningCommand {
	}
}
