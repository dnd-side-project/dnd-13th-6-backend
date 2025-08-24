package com.runky.goal.infrastructure;

import com.runky.goal.domain.MemberGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberGoalJpaRepository extends JpaRepository<MemberGoal, Long> {
}
