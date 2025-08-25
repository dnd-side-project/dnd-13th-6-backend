package com.runky.goal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CrewGoalSnapshotTest {

    @Test
    @DisplayName("목표가 비어있는 CrewGoalSnapshot을 생성한다.")
    void createEmptyCrewGoalSnapshot() {
        // given
        Long crewId = 1L;
        LocalDate date = LocalDate.of(2024, 6, 10);

        // when
        CrewGoalSnapshot snapshot = CrewGoalSnapshot.empty(crewId, date);

        // then
        assertNotNull(snapshot);
        assertEquals(crewId, snapshot.getCrewId());
        assertEquals(new BigDecimal("0.00"), snapshot.getGoal().value());
        assertFalse(snapshot.getAchieved());
        assertEquals(WeekUnit.from(date), snapshot.getWeekUnit());
    }
}