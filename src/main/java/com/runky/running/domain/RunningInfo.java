package com.runky.running.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class RunningInfo {
	private RunningInfo() {
	}

	public record Start(Long runningId, Long runnerId, String status, LocalDateTime startedAt) {
		public static Start from(Running running) {
			return new Start(
				running.getId(), running.getRunnerId(),
				running.getStatus().toString(), running.getStartedAt()
			);
		}
	}

	public record End(Long runningId, Long runnerId, Double distance, String status, LocalDateTime startedAt, LocalDateTime endedAt) {
	}

	public record TotalDistance(Long runnerId, Double totalDistance) {
	}

	public record RunningResult(Long runnerId, Double distance) {
	}

	public record RunnerStatus(Long runnerId, boolean isRunning) {
	}

	public record RunnerStatusAndSub(Long runnerId, boolean isRunning, String sub) {
	}

	public record TodaySummary(Double totalDistanceMeters, Long durationSeconds, Double avgSpeedMps) {
	}

	public record MyWeek(Long runnerId, Double totalMeters, LocalDate weekStart, LocalDate weekEnd) {
	}

	public record RunResult(
		Long runningId, Long runnerId,
		Double totalDistanceMeter, Long durationSeconds, Double avgSpeedMps,
		LocalDateTime startedAt, LocalDateTime endedAt,
		String format, String points
	) {
		public static RunResult from(final Running running, final RunningTrack runningTrack) {
			return new RunResult(
				running.getId(), running.getRunnerId(),
				running.getTotalDistanceMeter(), running.getDurationSeconds(), running.getAvgSpeedMPS(),
				running.getStartedAt(), running.getEndedAt(),
				runningTrack.getFormat(), runningTrack.getPoints()
			);
		}
	}

    public record History(
            Long id,
            Long runnerId,
            Running.Status status,
            Double distance,
            Long durationSeconds,
            Double avgSpeedMPS,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        public static History from(Running running) {
            return new History(
                    running.getId(),
                    running.getRunnerId(),
                    running.getStatus(),
                    running.getTotalDistanceMeter(),
                    running.getDurationSeconds(),
                    running.getAvgSpeedMPS(),
                    running.getStartedAt(),
                    running.getEndedAt()
            );
        }
    }
}
