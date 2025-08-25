package com.runky.goal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WeekUnitTest {

    @Test
    @DisplayName("WeekUnit은 ISO 연도와 주차로 생성된다.")
    void createWeekUnit_withIsoYearAndWeek() {
        WeekUnit weekUnit = WeekUnit.from(LocalDate.of(2025, 8, 24));

        assertThat(weekUnit.isoYear()).isEqualTo(2025);
        assertThat(weekUnit.isoWeek()).isEqualTo(34);
    }
}