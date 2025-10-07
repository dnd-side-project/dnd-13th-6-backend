package com.runky.goal.infrastructure;

import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.WeekUnit;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface CrewGoalSnapshotJpaRepository extends JpaRepository<CrewGoalSnapshot, Long> {

    @Query("SELECT cgs FROM CrewGoalSnapshot cgs WHERE cgs.crewId = :crewId AND cgs.weekUnit = :weekUnit")
    Optional<CrewGoalSnapshot> findCrewSnapshotOfWeek(Long crewId, WeekUnit weekUnit);

    @Query("SELECT cgs FROM CrewGoalSnapshot cgs WHERE cgs.crewId = :crewId AND cgs.weekUnit = :weekUnit")
    Optional<CrewGoalSnapshot> findSnapshotOf(Long crewId, WeekUnit weekUnit);

    @Query("SELECT cgs FROM CrewGoalSnapshot cgs WHERE cgs.crewId IN :crewIds AND cgs.weekUnit = :weekUnit")
    List<CrewGoalSnapshot> findAllSnapshotsOf(Set<Long> crewIds, WeekUnit weekUnit);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<CrewGoalSnapshot> findAllByCrewIdInAndWeekUnit(Set<Long> crewIds, WeekUnit weekUnit);
}
