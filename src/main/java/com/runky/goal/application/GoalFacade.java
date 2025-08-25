package com.runky.goal.application;

import com.runky.goal.domain.GoalCommand;
import com.runky.goal.domain.GoalService;
import com.runky.goal.domain.MemberGoalSnapshot;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GoalFacade {

    private final GoalService goalService;

    @Transactional(readOnly = true)
    public MemberGoalSnapshotResult getMemberGoalSnapshot(GoalCriteria.MemberGoal criteria) {
        MemberGoalSnapshot memberGoalSnapshot = goalService.getMemberGoalSnapshot(
                new GoalCommand.GetMemberSnapshot(criteria.memberId(), LocalDate.now()));
        return MemberGoalSnapshotResult.from(memberGoalSnapshot);
    }
}
