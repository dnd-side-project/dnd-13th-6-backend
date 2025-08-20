package com.runky.running.domain;

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
}
