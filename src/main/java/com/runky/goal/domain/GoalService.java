package com.runky.goal.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;

    @Transactional
    public void saveAllMemberSnapshots(GoalCommand.Snapshot command) {
        List<MemberGoal> memberGoals = goalRepository.findAllMemberGoals();

        List<MemberGoalSnapshot> snapshots = new ArrayList<>();
        for (MemberGoal memberGoal : memberGoals) {
            MemberGoalSnapshot snapshot = memberGoal.createSnapshot(command.date());
            snapshots.add(snapshot);
        }

        goalRepository.saveAll(snapshots);
    }

    @Transactional(readOnly = true)
    public CrewGoalSnapshot createCrewSnapshot(GoalCommand.CrewSnapshot command) {
        WeekUnit weekUnit = WeekUnit.from(command.localDate());
        List<MemberGoalSnapshot> snapshots = goalRepository.findLatestSnapshotsOfWeek(command.memberIds(), weekUnit);
        return CrewGoalSnapshot.of(snapshots, command.crewId(), command.localDate());
    }

    @Transactional
    public void saveAllCrewSnapshots(List<CrewGoalSnapshot> crewSnapshots) {
        goalRepository.saveAllCrewGoalSnapshots(crewSnapshots);
    }

    @Transactional(readOnly = true)
    public Optional<MemberGoalSnapshot> findMemberGoalSnapshot(Long memberId, LocalDate localDate) {
        return goalRepository.findMemberGoalSnapshotOfWeek(memberId, WeekUnit.from(localDate));
    }

    @Transactional(readOnly = true)
    public Optional<CrewGoalSnapshot> findCrewGoalSnapshot(Long crewId, LocalDate date) {
        return goalRepository.findCrewGoalSnapshot(crewId, WeekUnit.from(date));
    }

    @Transactional(readOnly = true)
    public MemberGoalSnapshot getMemberGoalSnapshot(GoalCommand.GetMemberSnapshot command) {
        return goalRepository.findMemberGoalSnapshotOfWeek(
                command.memberId(), WeekUnit.from(command.localDate()))
                .orElse(MemberGoalSnapshot.empty(command.memberId(), command.localDate()));
    }

    @Transactional(readOnly = true)
    public CrewGoalSnapshot getCrewGoalSnapshot(GoalCommand.GetCrewSnapshot command) {
        return goalRepository.findCrewGoalSnapshot(command.crewId(), WeekUnit.from(command.localDate()))
                .orElse(CrewGoalSnapshot.empty(command.crewId(), command.localDate()));
    }
}
