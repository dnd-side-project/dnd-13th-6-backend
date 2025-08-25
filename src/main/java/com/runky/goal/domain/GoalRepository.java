package com.runky.goal.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GoalRepository {

    MemberGoal save(MemberGoal memberGoal);

    MemberGoalSnapshot save(MemberGoalSnapshot memberGoalSnapshot);

    CrewGoalSnapshot save(CrewGoalSnapshot crewGoalSnapshot);

    void saveAll(List<MemberGoalSnapshot> snapshots);

    void saveAllCrewGoalSnapshots(List<CrewGoalSnapshot> snapshots);

    List<MemberGoal> findAllMemberGoals();

    Optional<CrewGoalSnapshot> findCrewGoalSnapshotOfWeek(Long crewId, WeekUnit weekUnit);

    Optional<CrewGoalSnapshot> findCrewGoalSnapshot(Long crewId, WeekUnit weekUnit);

    Optional<MemberGoalSnapshot> findMemberGoalSnapshotOfWeek(Long memberId, WeekUnit weekUnit);

    List<MemberGoalSnapshot> findLatestSnapshotsOfWeek(Set<Long> memberIds, WeekUnit weekUnit);

    Optional<MemberGoal> findMemberGoalByMemberId(Long memberId);
}
