package com.runky.goal.batch;

import com.runky.goal.domain.MemberGoalSnapshot;
import org.springframework.batch.item.ItemProcessor;

public class WeeklyMemberGoalProcessor implements ItemProcessor<MemberGoalSnapshot, MemberGoalAchieveInfo> {

    @Override
    public MemberGoalAchieveInfo process(MemberGoalSnapshot item) throws Exception {
        boolean achieved = item.isAchieved();
        return new MemberGoalAchieveInfo(item.getMemberId(), achieved);
    }
}
