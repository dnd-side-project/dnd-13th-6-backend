package com.runky.goal.domain.batch;

import com.runky.goal.domain.CrewGoalSnapshot;
import org.springframework.batch.item.ItemProcessor;

public class WeeklyCrewGoalSnapshotProcessor implements ItemProcessor<CrewGoalSnapshot, CrewGoalAchieveInfo> {

    @Override
    public CrewGoalAchieveInfo process(CrewGoalSnapshot item) throws Exception {
        return new CrewGoalAchieveInfo(item.getCrewId(), item.getAchieved());
    }
}
