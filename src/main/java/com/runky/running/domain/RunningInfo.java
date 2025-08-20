package com.runky.running.domain;

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
}
