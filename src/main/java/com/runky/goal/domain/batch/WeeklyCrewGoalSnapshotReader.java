package com.runky.goal.domain.batch;

import com.runky.goal.domain.CrewGoalSnapshot;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;

public class WeeklyCrewGoalSnapshotReader extends JpaPagingItemReader<CrewGoalSnapshot> implements
        ItemReader<CrewGoalSnapshot> {

    public WeeklyCrewGoalSnapshotReader(EntityManagerFactory emf) {
        super();
        setEntityManagerFactory(emf);
        setQueryString("SELECT cgs FROM CrewGoalSnapshot cgs");
        setPageSize(500);
    }
}
