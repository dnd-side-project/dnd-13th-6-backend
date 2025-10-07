package com.runky.goal.domain.batch;

import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.WeekUnit;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;

public class WeeklyCrewGoalSnapshotReader extends JpaPagingItemReader<CrewGoalSnapshot> implements
        ItemReader<CrewGoalSnapshot> {

    public WeeklyCrewGoalSnapshotReader(EntityManagerFactory emf, LocalDate date) {
        super();
        setEntityManagerFactory(emf);
        WeekUnit lastWeek = WeekUnit.from(date.minusWeeks(1));
        setQueryString("SELECT cgs FROM CrewGoalSnapshot cgs WHERE cgs.weekUnit = :weekUnit");
        setParameterValues(Map.of("weekUnit", lastWeek));
        setPageSize(500);
    }
}
