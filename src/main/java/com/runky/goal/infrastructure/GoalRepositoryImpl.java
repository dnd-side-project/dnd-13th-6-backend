package com.runky.goal.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.GoalRepository;
import com.runky.goal.domain.MemberGoal;
import com.runky.goal.domain.MemberGoalSnapshot;
import com.runky.goal.domain.WeekUnit;

import lombok.RequiredArgsConstructor;

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
	public Optional<CrewGoalSnapshot> findCrewGoalSnapshotOfWeek(Long crewId, WeekUnit weekUnit) {
		return crewGoalSnapshotJpaRepository.findCrewSnapshotOfWeek(crewId, weekUnit);
	}

	@Override
	public Optional<CrewGoalSnapshot> findCrewGoalSnapshot(Long crewId, WeekUnit weekUnit) {
		return crewGoalSnapshotJpaRepository.findSnapshotOf(crewId, weekUnit);
	}

	@Override
	public Optional<MemberGoalSnapshot> findMemberGoalSnapshotOfWeek(Long memberId, WeekUnit weekUnit) {
		return memberGoalSnapshotJpaRepository.findSnapshotOf(memberId, weekUnit);
	}

	@Override
	public List<MemberGoalSnapshot> findLatestSnapshotsOfWeek(Set<Long> memberIds, WeekUnit weekUnit) {
		return memberGoalSnapshotJpaRepository.findSnapshotsOf(memberIds, weekUnit);
	}

	@Override
	public Optional<MemberGoal> findMemberGoalByMemberId(Long memberId) {
		return memberGoalJpaRepository.findByMemberId(memberId);
	}

	@Override
	public List<CrewGoalSnapshot> findAllCrewGoalSnapshots(Set<Long> crewId, WeekUnit weekUnit) {
		return crewGoalSnapshotJpaRepository.findAllSnapshotsOf(crewId, weekUnit);
	}

	@Override
	public boolean existsMemberGoalSnapShot(final Long memberId) {
		return memberGoalSnapshotJpaRepository.existsByMemberId(memberId);

	}

    @Override
    public Optional<MemberGoalSnapshot> findSnapshotWithLock(Long memberId, WeekUnit weekUnit) {
        return memberGoalSnapshotJpaRepository.findByMemberIdAndWeekUnit(memberId, weekUnit);
    }

    @Override
    public List<CrewGoalSnapshot> findAllCrewSnapshotsWithLock(Set<Long> crewIds, WeekUnit weekUnit) {
        return crewGoalSnapshotJpaRepository.findAllByCrewIdInAndWeekUnit(crewIds, weekUnit);
    }

    @Override
	public MemberGoal save(MemberGoal memberGoal) {
		return memberGoalJpaRepository.save(memberGoal);
	}

	@Override
	public MemberGoalSnapshot save(MemberGoalSnapshot memberGoalSnapshot) {
		return memberGoalSnapshotJpaRepository.save(memberGoalSnapshot);
	}

	@Override
	public CrewGoalSnapshot save(CrewGoalSnapshot crewGoalSnapshot) {
		return crewGoalSnapshotJpaRepository.save(crewGoalSnapshot);
	}

	@Override
	public void saveAll(List<MemberGoalSnapshot> snapshots) {
        String sql = "INSERT INTO member_goal_snapshot "
                + "(member_id, goal, run_distance, achieved, iso_year, iso_week, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW()) "
                + "ON DUPLICATE KEY UPDATE "
                + "goal = VALUES(goal), "
                + "run_distance = VALUES(run_distance), "
                + "achieved = VALUES(achieved), "
                + "updated_at = NOW()";
        jdbcTemplate.batchUpdate(sql, snapshots, snapshots.size(), (ps, snapshot) -> {
            ps.setLong(1, snapshot.getMemberId());
            ps.setBigDecimal(2, snapshot.getGoal().value());
            ps.setBigDecimal(3, snapshot.getRunDistance());
            ps.setBoolean(4, snapshot.getAchieved());
            ps.setInt(5, snapshot.getWeekUnit().isoYear());
            ps.setInt(6, snapshot.getWeekUnit().isoWeek());
        });
	}

	@Override
	public void saveAllCrewGoalSnapshots(List<CrewGoalSnapshot> snapshots) {
		String sql = "INSERT INTO crew_goal_snapshot "
			+ "(crew_id, goal, run_distance, achieved, iso_year, iso_week, created_at, updated_at) "
			+ "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW()) "
                + "ON DUPLICATE KEY UPDATE "
                + "goal = VALUES(goal), "
                + "run_distance = VALUES(run_distance), "
                + "achieved = VALUES(achieved), "
                + "updated_at = NOW()";
		jdbcTemplate.batchUpdate(sql, snapshots, snapshots.size(), (ps, snapshot) -> {
			ps.setLong(1, snapshot.getCrewId());
			ps.setBigDecimal(2, snapshot.getGoal().value());
            ps.setBigDecimal(3, snapshot.getRunDistance());
			ps.setBoolean(4, snapshot.getAchieved());
			ps.setInt(5, snapshot.getWeekUnit().isoYear());
			ps.setInt(6, snapshot.getWeekUnit().isoWeek());
		});
	}

}
