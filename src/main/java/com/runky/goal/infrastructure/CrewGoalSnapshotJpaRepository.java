package com.runky.goal.infrastructure;

import com.runky.goal.domain.CrewGoalSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CrewGoalSnapshotJpaRepository extends JpaRepository<CrewGoalSnapshot, Long> {

    @Query("SELECT cgs FROM CrewGoalSnapshot cgs WHERE cgs.crewId = :crewId ORDER BY cgs.weekUnit.isoYear DESC, cgs.weekUnit.isoWeek DESC")
    CrewGoalSnapshot findLatestSnapshot(Long crewId);
}
