package com.runky.reward.infrastructure;

import com.runky.goal.domain.WeekUnit;
import com.runky.reward.domain.MemberCloverHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCloverHistoryJpaRepository extends JpaRepository<MemberCloverHistory, Long> {
    Optional<MemberCloverHistory> findByMemberIdAndWeekUnit(Long userId, WeekUnit weekUnit);
}
