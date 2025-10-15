package com.runky.running.application;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.runky.running.domain.RunningCommand;

public final class RunningCriteria {
	private RunningCriteria() {
	}

	public record Start(Long runnerId) {
		RunningCommand.Start toCommand() {
			return new RunningCommand.Start(runnerId);
		}
	}

	public record End(
		Long runningId,
		Long runnerId,
		Double totalDistanceMeter,
		Long durationSeconds,
		Double avgSpeedMPS,
		String format,
		String points,
		int pointCount

	) {
		public RunningCommand.End toCommand() {
			return new RunningCommand.End(
				runningId, runnerId, totalDistanceMeter, durationSeconds, avgSpeedMPS, format, points, pointCount
			);
		}
	}

	public record EndWithNoRunningId(
		Long runnerId,
		Double totalDistanceMeter,
		Long durationSeconds,
		Double avgSpeedMPS,
		String format,
		String points,
		int pointCount

	) {
		public RunningCommand.End toCommand(Long runningId) {
			return new RunningCommand.End(
				runningId, runnerId, totalDistanceMeter, durationSeconds, avgSpeedMPS, format, points, pointCount
			);
		}
	}

	public record TodaySummary(Long runnerId, LocalDateTime now) {
	}

	public record MyWeeklyTotalDistance(Long runnerId) {
	}

	public record RunResult(Long runnerId, Long runningId) {
	}

	public record RemoveActiveRunning(Long runnerId, Long runningId) {
	}

	public record Weekly(Long runnerId, LocalDate start) {
	}

	public record Monthly(Long runnerId, int year, int month) {
	}
}
