package com.runky.goal.infrastructure;

import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.GoalRepository;
import com.runky.goal.domain.MemberGoal;
import com.runky.goal.domain.MemberGoalSnapshot;
import com.runky.goal.domain.WeekUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GoalRepositoryImpl implements GoalRepository {

    private final MemberGoalJpaRepository memberGoalJpaRepository;
    private final MemberGoalSnapshotJpaRepository memberGoalSnapshotJpaRepository;
    private final CrewGoalSnapshotJpaRepository crewGoalSnapshotJpaRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<MemberGoal> findAllMemberGoals() {
        return memberGoalJpaRepository.findAll();
    }

    @Override
    public Optional<MemberGoalSnapshot> findLatestMemberGoalSnapshot(Long memberId) {
        return Optional.ofNullable(memberGoalSnapshotJpaRepository.findLatestSnapshot(memberId));
    }

    @Override
    public Optional<CrewGoalSnapshot> findLatestCrewGoalSnapshot(Long crewId) {
        return Optional.ofNullable(crewGoalSnapshotJpaRepository.findLatestSnapshot(crewId));
    }

    @Override
    public List<MemberGoalSnapshot> findLatestSnapshots(Set<Long> memberIds, WeekUnit weekUnit) {
        return memberGoalSnapshotJpaRepository.findSnapshotsOf(memberIds, weekUnit);
    }

    @Override
    public void save(MemberGoal memberGoal) {
        memberGoalJpaRepository.save(memberGoal);
    }

    @Override
    public void saveAll(List<MemberGoalSnapshot> snapshots) {
        String sql = "INSERT INTO member_goal_snapshot "
                + "(member_id, goal, achieved, iso_year, iso_week, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        jdbcTemplate.batchUpdate(sql, snapshots, snapshots.size(), (ps, snapshot) -> {
            ps.setLong(1, snapshot.getMemberId());
            ps.setBigDecimal(2, snapshot.getGoal().value());
            ps.setBoolean(3, snapshot.getAchieved());
            ps.setInt(4, snapshot.getWeekUnit().isoYear());
            ps.setInt(5, snapshot.getWeekUnit().isoWeek());
        });
    }

    @Override
    public void saveAllCrewGoalSnapshots(List<CrewGoalSnapshot> snapshots) {
        String sql = "INSERT INTO crew_goal_snapshot "
                + "(crew_id, goal, achieved, iso_year, iso_week, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        jdbcTemplate.batchUpdate(sql, snapshots, snapshots.size(), (ps, snapshot) -> {
            ps.setLong(1, snapshot.getCrewId());
            ps.setBigDecimal(2, snapshot.getGoal().value());
            ps.setBoolean(3, snapshot.getAchieved());
            ps.setInt(4, snapshot.getWeekUnit().isoYear());
            ps.setInt(5, snapshot.getWeekUnit().isoWeek());
        });
    }
}
