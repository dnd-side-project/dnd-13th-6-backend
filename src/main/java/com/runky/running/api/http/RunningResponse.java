package com.runky.running.api.http;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.runky.running.application.RunningResult;

public sealed interface RunningResponse {

	record Start(Long runningId, Long runnerId, String status, String pub, String sub,
				 LocalDateTime startedAt)
		implements RunningResponse {
		public static Start from(String pub, String sub, RunningResult.Start result) {
			return new Start(result.runningId(), result.runnerId(), result.status(), pub, sub, result.startedAt());
		}
	}

	record End(Long runningId, Long runnerId, String string, LocalDateTime startedAt, LocalDateTime endedAt)
		implements RunningResponse {
		public static End from(RunningResult.End result) {
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

	record RunResult(
		Long runningId, Long runnerId,
		Double totalDistanceMeter, Long durationSeconds, Double avgSpeedMps,
		LocalDateTime startedAt, LocalDateTime endedAt,
		String format, String points
	) {
		public static RunResult from(RunningResult.RunResult r) {
			return new RunResult(
				r.runningId(), r.runnerId(),
				r.totalDistanceMeter(), r.durationSeconds(), r.avgSpeedMps(),
				r.startedAt(), r.endedAt(),
				r.format(), r.points()
			);
		}

	}
}
