package com.runky.running.application;

import java.time.LocalDateTime;

public class RunningEvent {
    public record Ended(
            Long runningId,
            Long runnerId,
            String status,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
    }
}
