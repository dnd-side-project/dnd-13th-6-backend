package com.runky.running.application;

import java.time.LocalDateTime;

import com.runky.running.domain.RunningCommand;

public sealed interface RunningCriteria {

	record Start(
		Long runnerId
	) {
		RunningCommand.Start toCommand() {
			return new RunningCommand.Start(runnerId);
		}
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

	) implements RunningCriteria {
		RunningCommand.End toCommand() {
			return new RunningCommand.End(
				runningId, runnerId, totalDistanceMinutes, durationSeconds, avgSpeedMPS, format, points, pointCount
			);
		}
	}

	record TodaySummary(Long runnerId, LocalDateTime now) implements RunningCriteria {
	}

	record MyWeeklyTotalDistance(Long runnerId) implements RunningCriteria {
	}
}
