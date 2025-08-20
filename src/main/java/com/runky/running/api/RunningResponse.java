package com.runky.running.api;

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
}
