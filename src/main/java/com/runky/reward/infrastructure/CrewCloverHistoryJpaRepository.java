package com.runky.reward.infrastructure;

import com.runky.goal.domain.WeekUnit;
import com.runky.reward.domain.CrewCloverHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewCloverHistoryJpaRepository extends JpaRepository<CrewCloverHistory, Long> {
    Optional<CrewCloverHistory> findByCrewIdAndWeekUnit(Long crewId, WeekUnit weekUnit);
}
