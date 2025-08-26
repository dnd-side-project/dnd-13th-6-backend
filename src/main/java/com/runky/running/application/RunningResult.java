package com.runky.running.application;

import java.time.LocalDateTime;

import com.runky.running.domain.RunningInfo;

public sealed interface RunningResult {

	record Start(Long runningId, Long runnerId, String status, LocalDateTime startedAt) implements RunningResult {
		public static Start from(RunningInfo.Start info) {
			return new Start(
				info.runningId(),
				info.runnerId(),
				info.status(),
				info.startedAt()
			);
		}
	}

	record End(Long runningId, Long runnerId, String status, LocalDateTime startedAt, LocalDateTime endedAt
	) implements RunningResult {

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

	record TodaySummary(Double totalDistanceMeters, Long durationSeconds, Double avgSpeedMps) {
		public static TodaySummary from(RunningInfo.TodaySummary info) {
			return new TodaySummary(info.totalDistanceMeters(), info.durationSeconds(), info.avgSpeedMps());
		}

	}
}
