package com.runky.running.application;

import java.time.LocalDateTime;

public class RunningEvent {
    public record Ended(
            Long runningId,
            Long runnerId,
            Double distance,
            String status,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
    }
}
