package com.runky.goal.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.runky.goal.domain.MemberGoal;

public interface MemberGoalJpaRepository extends JpaRepository<MemberGoal, Long> {

	Optional<MemberGoal> findByMemberId(Long memberId);

	void deleteByMemberId(Long memberId);
}
