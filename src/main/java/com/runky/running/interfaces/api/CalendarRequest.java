package com.runky.running.interfaces.api;

import java.time.LocalDate;

public class CalendarRequest {
    public record Weekly(
            LocalDate date
    ) {
    }
}
