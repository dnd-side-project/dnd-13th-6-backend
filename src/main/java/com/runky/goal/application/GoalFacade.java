package com.runky.goal.application;

import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.GoalCommand;
import com.runky.goal.domain.GoalService;
import com.runky.goal.domain.MemberGoal;
import com.runky.goal.domain.MemberGoalSnapshot;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoalFacade {

    private final GoalService goalService;

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
}
