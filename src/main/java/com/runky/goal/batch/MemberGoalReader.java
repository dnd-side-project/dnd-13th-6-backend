package com.runky.goal.batch;

import com.runky.goal.domain.MemberGoal;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;

public class MemberGoalReader extends JpaPagingItemReader<MemberGoal> implements ItemReader<MemberGoal> {
    public MemberGoalReader(EntityManagerFactory emf) {
        super();
        setEntityManagerFactory(emf);
        setQueryString("SELECT m FROM MemberGoal m");
        setPageSize(500);
    }
}
