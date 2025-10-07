package com.runky.goal.domain;

import com.runky.global.error.GlobalErrorCode;
import com.runky.global.error.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Transactional
    public void updateDistances(GoalCommand.UpdateDistance cmd) {
        MemberGoalSnapshot memberSnapshot = goalRepository.findSnapshotWithLock(cmd.memberId(), WeekUnit.from(cmd.date()))
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND));
        memberSnapshot.addDistance(cmd.distance());
        List<CrewGoalSnapshot> crewSnapshots = goalRepository.findAllCrewSnapshotsWithLock(cmd.crewIds(), WeekUnit.from(cmd.date()));
        for (CrewGoalSnapshot crewSnapshot : crewSnapshots) {
            crewSnapshot.addDistance(cmd.distance());
        }
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

    @Transactional
    public MemberGoal updateMemberGoal(GoalCommand.Update command) {
        MemberGoal memberGoal = goalRepository.findMemberGoalByMemberId(command.memberId())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOT_FOUND));

        boolean firstSnapShot = !goalRepository.existsMemberGoalSnapShot(command.memberId());

        memberGoal.updateGoal(command.goal());

        if (firstSnapShot) {
            goalRepository.save(memberGoal.createSnapshot(LocalDate.now()));
        }
        return memberGoal;
    }

    @Transactional(readOnly = true)
    public List<CrewGoalSnapshot> getAllLastWeekCrewGoalSnapshots(GoalCommand.CrewSnapshots command) {
        return goalRepository.findAllCrewGoalSnapshots(command.crewIds(),
                WeekUnit.from(command.localDate().minusWeeks(1)));
    }

    @Transactional
    public void init(GoalCommand.Init command) {
        MemberGoal memberGoal = MemberGoal.from(command.memberId());
        goalRepository.save(memberGoal);
    }
}
