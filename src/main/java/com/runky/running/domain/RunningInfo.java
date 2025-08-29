package com.runky.running.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public sealed interface RunningInfo {

	record Start(Long runningId, Long runnerId, String status, LocalDateTime startedAt) implements RunningInfo {
		static Start from(Running running) {
			return new Start(
				running.getId(),
				running.getRunnerId(),
				running.getStatus().toString(),
				running.getStartedAt()
			);
		}
	}

	record End(Long runningId, Long runnerId, String status, LocalDateTime startedAt, LocalDateTime endedAt)
		implements RunningInfo {
	}

	record TotalDistance(Long runnerId, Double totalDistance) implements RunningInfo {
	}

	record RunningResult(Long runnerId, Double distance) implements RunningInfo {
	}

	record RunnerStatus(Long runnerId, boolean isRunning) implements RunningInfo {
	}

	record RunnerStatusAndSub(Long runnerId, boolean isRunning, String sub) implements RunningInfo {
	}

	record TodaySummary(Double totalDistanceMeters, Long durationSeconds, Double avgSpeedMps) {
	}

	record MyWeek(Long runnerId, Double totalMeters, LocalDate weekStart, LocalDate weekEnd) {
	}

	record RunResult(
		Long runningId, Long runnerId,
		Double totalDistanceMeter, Long durationSeconds, Double avgSpeedMps,
		LocalDateTime startedAt, LocalDateTime endedAt,
		String format, String points
	) implements RunningInfo {
		static RunResult from(final Running running, final RunningTrack runningTrack) {
			return new RunResult(
				running.getId(), running.getRunnerId(),
				running.getTotalDistanceMeter(), running.getDurationSeconds(), running.getAvgSpeedMPS(),
				running.getStartedAt(), running.getEndedAt(),
				runningTrack.getFormat(), runningTrack.getPoints()
			);
		}
	}
}
