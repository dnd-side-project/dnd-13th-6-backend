package com.runky.goal.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GoalRepository {

    void save(MemberGoal memberGoal);

    void save(MemberGoalSnapshot memberGoalSnapshot);

    void saveAll(List<MemberGoalSnapshot> snapshots);

    void saveAllCrewGoalSnapshots(List<CrewGoalSnapshot> snapshots);

    List<MemberGoal> findAllMemberGoals();

    Optional<MemberGoalSnapshot> findLatestMemberGoalSnapshot(Long memberId);

    Optional<CrewGoalSnapshot> findLatestCrewGoalSnapshot(Long crewId);

    Optional<MemberGoalSnapshot> findMemberGoalSnapshotOfWeek(Long memberId, WeekUnit weekUnit);

    List<MemberGoalSnapshot> findLatestSnapshots(Set<Long> memberIds, WeekUnit weekUnit);
}
