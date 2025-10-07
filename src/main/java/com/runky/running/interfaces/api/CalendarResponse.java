package com.runky.running.interfaces.api;

import com.runky.running.application.RunningResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarResponse {
    public record Histories(
            Double totalDistance,
            Long totalDuration,
            List<History> histories
    ) {
        public static Histories from(List<RunningResult.History> histories) {
            Double totalDistance = 0.0;
            Long totalDuration = 0L;
            List<History> response = new ArrayList<>();

            for (RunningResult.History history : histories) {
                totalDistance += history.distance();
                totalDuration += history.durationSeconds();
                response.add(History.from(history));
            }

            return new Histories(totalDistance, totalDuration, response);
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
