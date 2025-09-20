package com.runky.running.domain;

import java.time.LocalDateTime;

public final class RunningCommand {
	private RunningCommand() {
	}

	public record Start(Long runnerId) {
	}

	public record End(
		Long runningId, Long runnerId,
		Double totalDistanceMinutes, Long durationSeconds, Double avgSpeedMPS,
		String format, String points, int pointCount
	) {
	}

	public record WeekDistance(Long runnerId, LocalDateTime from, LocalDateTime to) {
	}

	public record MyWeeklyTotalDistance(Long runnerId) {
	}

	public record RunResult(Long runnerId, Long runningId) {
	}

	public record RemoveActiveRunning(Long runnerId, Long runningId) {
	}
}
