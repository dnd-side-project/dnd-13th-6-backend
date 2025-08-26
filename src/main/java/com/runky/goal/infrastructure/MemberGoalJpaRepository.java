package com.runky.goal.infrastructure;

import com.runky.goal.domain.MemberGoal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberGoalJpaRepository extends JpaRepository<MemberGoal, Long> {

    Optional<MemberGoal> findByMemberId(Long memberId);
}
