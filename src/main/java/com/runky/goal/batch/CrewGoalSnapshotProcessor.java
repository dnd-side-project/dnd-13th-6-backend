package com.runky.goal.batch;

import com.runky.goal.domain.CrewGoalSnapshot;
import org.springframework.batch.item.ItemProcessor;

public class CrewGoalSnapshotProcessor implements ItemProcessor<CrewGoalSnapshot, CrewGoalAchieveInfo> {

    @Override
    public CrewGoalAchieveInfo process(CrewGoalSnapshot item) throws Exception {
        return new CrewGoalAchieveInfo(item.getCrewId(), item.getAchieved());
    }
}
