package com.runky.goal.batch;

import com.runky.goal.domain.MemberGoalSnapshot;
import com.runky.goal.domain.WeekUnit;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;

public class WeeklyMemberGoalReader extends JpaPagingItemReader<MemberGoalSnapshot> implements
        ItemReader<MemberGoalSnapshot> {

    public WeeklyMemberGoalReader(EntityManagerFactory emf,
                                  LocalDate date) {
        super();
        setEntityManagerFactory(emf);
        WeekUnit lastWeek = WeekUnit.from(date.minusWeeks(1));
        setQueryString("SELECT m FROM MemberGoalSnapshot m "
                + "WHERE m.weekUnit = :weekUnit");
        setParameterValues(Map.of("weekUnit", lastWeek));
        setPageSize(500);
    }
}
