package com.runky.running.application;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.runky.running.domain.RunningInfo;

public final class RunningResult {
	private RunningResult() {
	}

	public record RemovedRunning(int count) {
	}

	public record Start(Long runningId, Long runnerId, String status, LocalDateTime startedAt) {
		public static Start from(RunningInfo.Start info) {
			return new Start(
				info.runningId(),
				info.runnerId(),
				info.status(),
				info.startedAt()
			);
		}
	}

	public record End(Long runningId, Long runnerId, String status, LocalDateTime startedAt, LocalDateTime endedAt
	) {

		public static End from(RunningInfo.End info) {
			return new End(
				info.runningId(),
				info.runnerId(),
				info.status(),
				info.startedAt(),
				info.endedAt()
			);
		}
	}

	public record TodaySummary(Double totalDistanceMeters, Long durationSeconds, Double avgSpeedMps) {
		public static TodaySummary from(RunningInfo.TodaySummary info) {
			return new TodaySummary(info.totalDistanceMeters(), info.durationSeconds(), info.avgSpeedMps());
		}

	}

	public record MyWeeklyTotalDistance(
		Long runnerId,
		double totalDistanceMeter,
		LocalDate weekStart,
		LocalDate weekEnd
	) {
		public static MyWeeklyTotalDistance from(RunningInfo.MyWeek info) {
			return new MyWeeklyTotalDistance(info.runnerId(), info.totalMeters(), info.weekStart(), info.weekEnd());
		}
	}

	public record RunResult(
		Long runningId, Long runnerId,
		Double totalDistanceMeter, Long durationSeconds, Double avgSpeedMps,
		LocalDateTime startedAt, LocalDateTime endedAt,
		String format, String points
	) {
		public static RunResult from(RunningInfo.RunResult info) {
			return new RunResult(
				info.runningId(), info.runnerId(),
				info.totalDistanceMeter(), info.durationSeconds(), info.avgSpeedMps(),
				info.startedAt(), info.endedAt(),
				info.format(), info.points()
			);
		}
	}
}
