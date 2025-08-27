package com.runky.running.api;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.runky.running.application.RunningResult;

public sealed interface RunningResponse {

	record Start(Long runningId, Long runnerId, String status, String publishDestination, LocalDateTime startedAt)
		implements RunningResponse {
		static Start from(String pub, RunningResult.Start result) {
			return new Start(result.runningId(), result.runnerId(), result.status(), pub, result.startedAt());
		}
	}

	record End(Long runningId, Long runnerId, String string, LocalDateTime startedAt, LocalDateTime endedAt)
		implements RunningResponse {
		static End from(RunningResult.End result) {
			return new End(result.runningId(), result.runnerId(), result.status(), result.startedAt(),
				result.endedAt());
		}
	}

	record TodaySummary(Double totalDistanceMeter, Long durationSeconds, Double avgSpeedMps) {
		public static TodaySummary from(RunningResult.TodaySummary r) {
			return new RunningResponse.TodaySummary(r.totalDistanceMeters(), r.durationSeconds(), r.avgSpeedMps());
		}
	}

	record MyWeeklyTotalDistance(
		double totalDistanceKm,
		double totalDistanceMeter,
		LocalDate weekStart,
		LocalDate weekEnd
	) {
		public static MyWeeklyTotalDistance from(RunningResult.MyWeeklyTotalDistance r) {
			return new MyWeeklyTotalDistance(r.totalDistanceKm(), r.totalDistanceMeter(), r.weekStart(), r.weekEnd());
		}
	}
}
