package com.runky.goal.infrastructure;

import com.runky.goal.domain.MemberGoalSnapshot;
import com.runky.goal.domain.WeekUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberGoalSnapshotJpaRepository extends JpaRepository<MemberGoalSnapshot, Long> {

    @Query("SELECT mgs FROM MemberGoalSnapshot mgs WHERE mgs.memberId = :memberId ORDER BY mgs.weekUnit.isoYear DESC, mgs.weekUnit.isoWeek DESC "
            + "LIMIT 1")
    MemberGoalSnapshot findLatestSnapshot(Long memberId);

    @Query("SELECT mgs FROM MemberGoalSnapshot mgs WHERE mgs.memberId IN :memberIds AND mgs.weekUnit = :weekUnit")
    List<MemberGoalSnapshot> findSnapshotsOf(Set<Long> memberIds, WeekUnit weekUnit);

    @Query("SELECT mgs FROM MemberGoalSnapshot mgs WHERE mgs.memberId = :memberId AND mgs.weekUnit = :weekUnit")
    Optional<MemberGoalSnapshot> findSnapshotOf(Long memberId, WeekUnit weekUnit);
}
