package com.runky.goal.batch;

import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.Goal;
import com.runky.goal.domain.WeekUnit;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

@RequiredArgsConstructor
public class CrewGoalProcessor implements ItemProcessor<CrewGoalSum, CrewGoalSnapshot> {
    private final LocalDate date;

    @Override
    public CrewGoalSnapshot process(CrewGoalSum item) throws Exception {
        return new CrewGoalSnapshot(item.crewId(), new Goal(item.totalGoal()), false, WeekUnit.from(date));

    }
}
