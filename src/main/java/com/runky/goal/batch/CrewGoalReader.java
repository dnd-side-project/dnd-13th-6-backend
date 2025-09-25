package com.runky.goal.batch;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;

public class CrewGoalReader extends JpaPagingItemReader<CrewGoalSum> implements ItemReader<CrewGoalSum> {
    public CrewGoalReader(EntityManagerFactory emf) {
        super();
        setEntityManagerFactory(emf);
        setQueryString("SELECT new com.runky.goal.batch.CrewGoalSum(cm.crew.id, SUM(mg.goal.value))" +
                "FROM CrewMember cm " +
                "JOIN MemberGoal mg ON cm.memberId = mg.memberId " +
                "WHERE cm.role IN (com.runky.crew.domain.CrewMember.Role.MEMBER, com.runky.crew.domain.CrewMember.Role.LEADER) " +
                "GROUP BY cm.crew.id");
        setPageSize(300);

    }
}
