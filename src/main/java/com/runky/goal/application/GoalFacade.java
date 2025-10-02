package com.runky.goal.application;

import com.runky.crew.domain.Crew;
import com.runky.crew.domain.CrewService;
import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.GoalCommand;
import com.runky.goal.domain.GoalService;
import com.runky.goal.domain.MemberGoal;
import com.runky.goal.domain.MemberGoalSnapshot;
import com.runky.reward.domain.RewardCommand;
import com.runky.reward.domain.RewardService;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoalFacade {

    private final GoalService goalService;
    private final RewardService rewardService;
    private final CrewService crewService;

    public MemberGoalSnapshotResult getMemberGoalSnapshot(GoalCriteria.MemberGoal criteria) {
        MemberGoalSnapshot memberGoalSnapshot = goalService.getMemberGoalSnapshot(
                new GoalCommand.GetMemberSnapshot(criteria.memberId(), LocalDate.now()));
        return MemberGoalSnapshotResult.from(memberGoalSnapshot);
    }

    public CrewGoalSnapshotResult getCrewGoalSnapshot(GoalCriteria.CrewGoal criteria) {
        CrewGoalSnapshot snapshot = goalService.getCrewGoalSnapshot(
                new GoalCommand.GetCrewSnapshot(criteria.crewId(), LocalDate.now()));
        return CrewGoalSnapshotResult.from(snapshot);
    }

    public MemberGoalResult updateMemberGoal(GoalCriteria.Update criteria) {
        MemberGoal goal = goalService.updateMemberGoal(new GoalCommand.Update(criteria.memberId(), criteria.goal()));
        return MemberGoalResult.from(goal);
    }

    public MemberGoalSnapshotResult getLastWeekMemberGoalSnapshot(GoalCriteria.MemberGoal criteria) {
        MemberGoalSnapshot memberGoalSnapshot = goalService.getMemberGoalSnapshot(
                new GoalCommand.GetMemberSnapshot(criteria.memberId(), LocalDate.now().minusWeeks(1)));
        return MemberGoalSnapshotResult.from(memberGoalSnapshot);
    }

    public CrewGoalSnapshotResult getLastWeekCrewGoalSnapshot(GoalCriteria.CrewGoal criteria) {
        CrewGoalSnapshot snapshot = goalService.getCrewGoalSnapshot(
                new GoalCommand.GetCrewSnapshot(criteria.crewId(), LocalDate.now().minusWeeks(1)));
        return CrewGoalSnapshotResult.from(snapshot);
    }

    public MemberGoalSnapshotResult.Clover getLastWeekMemberGoalClover(GoalCriteria.LastWeekClover criteria) {
        MemberGoalSnapshot memberGoalSnapshot = goalService.getMemberGoalSnapshot(
                new GoalCommand.GetMemberSnapshot(criteria.memberId(), LocalDate.now().minusWeeks(1)));
        if (!memberGoalSnapshot.getAchieved()) {
            return new MemberGoalSnapshotResult.Clover(0L);
        }
        long clover = rewardService.calculateMemberGoalClover(new RewardCommand.Count(1L));
        return new MemberGoalSnapshotResult.Clover(clover);
    }

    public CrewGoalSnapshotResult.Clover getLastWeekCrewGoalClover(GoalCriteria.LastWeekClover criteria) {
        List<Crew> crews = crewService.getCrewsOfUser(criteria.memberId());

        Set<Long> crewIds = crews.stream()
                .map(Crew::getId)
                .collect(Collectors.toSet());

        GoalCommand.CrewSnapshots command = new GoalCommand.CrewSnapshots(crewIds, LocalDate.now());
        List<CrewGoalSnapshot> achievedSnapshots = goalService.getAllLastWeekCrewGoalSnapshots(command).stream()
                .filter(CrewGoalSnapshot::getAchieved)
                .toList();

        long count = rewardService.calculateCrewGoalClover(new RewardCommand.Count((long) achievedSnapshots.size()));

        return new CrewGoalSnapshotResult.Clover(count);
    }

    public void updateSnapshots(GoalCriteria.UpdateDistance cri) {
        Set<Long> crewIds = crewService.getCrewsOfUser(cri.memberId()).stream()
                .map(Crew::getId)
                .collect(Collectors.toSet());

        goalService.updateDistances(new GoalCommand.UpdateDistance(cri.memberId(), crewIds, cri.distance(), cri.date()));
    }

    public void init(GoalCriteria.Init cri) {
        goalService.init(new GoalCommand.Init(cri.memberId()));
    }
}
