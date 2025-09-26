package com.runky.running.interfaces.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.runky.running.application.RunningResult;

public final class RunningResponse {
	private RunningResponse() {
	}

	public record RemovedRunning(int count) {

		public static RemovedRunning from(RunningResult.RemovedRunning result) {
			return new RemovedRunning(result.count());
		}
	}

	public record Start(Long runningId, Long runnerId, String status, String pub, String sub,
						LocalDateTime startedAt) {
		public static Start from(String pub, String sub, RunningResult.Start result) {
			return new Start(result.runningId(), result.runnerId(), result.status(), pub, sub, result.startedAt());
		}
	}

	public record End(Long runningId, Long runnerId, String string, LocalDateTime startedAt, LocalDateTime endedAt) {
		public static End from(RunningResult.End result) {
			return new End(result.runningId(), result.runnerId(), result.status(), result.startedAt(),
				result.endedAt());
		}
	}

	public record TodaySummary(Double totalDistanceMeter, Long durationSeconds, Double avgSpeedMps) {
		public static TodaySummary from(RunningResult.TodaySummary r) {
			return new RunningResponse.TodaySummary(r.totalDistanceMeters(), r.durationSeconds(), r.avgSpeedMps());
		}
	}

	public record MyWeeklyTotalDistance(
		double totalDistanceKm,
		double totalDistanceMeter,
		LocalDate weekStart,
		LocalDate weekEnd
	) {
		public static MyWeeklyTotalDistance from(RunningResult.MyWeeklyTotalDistance r) {
			BigDecimal km = BigDecimal.valueOf(r.totalDistanceMeter())
				.divide(new BigDecimal("1000"));
			double totalDistanceKm = km.setScale(2, RoundingMode.DOWN).doubleValue();
			return new MyWeeklyTotalDistance(totalDistanceKm, r.totalDistanceMeter(), r.weekStart(), r.weekEnd());
		}
	}

	public record RunResult(
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
