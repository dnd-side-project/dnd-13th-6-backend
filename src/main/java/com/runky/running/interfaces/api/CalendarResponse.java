package com.runky.running.interfaces.api;

import com.runky.running.application.RunningResult;

import java.time.LocalDate;
import java.util.List;

public class CalendarResponse {
    public record Weekly(
            Double totalDistance,
            Long totalDuration,
            List<History> histories
    ) {
        public static Weekly from(List<RunningResult.History> histories) {
            Double totalDistance = histories.stream()
                    .mapToDouble(RunningResult.History::distance)
                    .sum();
            Long totalDuration = histories.stream()
                    .mapToLong(RunningResult.History::durationSeconds)
                    .sum();
            List<History> historyResponses = histories.stream()
                    .map(History::from)
                    .toList();

            return new Weekly(totalDistance, totalDuration, historyResponses);
        }
    }

    public record History(
            LocalDate date,
            Double distance,
            Long duration
    ) {
        public static History from(RunningResult.History history) {
            return new History(
                    history.endedAt().toLocalDate(),
                    history.distance(),
                    history.durationSeconds()
            );
        }
    }
}
