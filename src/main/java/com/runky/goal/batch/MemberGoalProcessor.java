package com.runky.goal.batch;

import com.runky.goal.domain.MemberGoal;
import com.runky.goal.domain.MemberGoalSnapshot;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

@RequiredArgsConstructor
public class MemberGoalProcessor implements ItemProcessor<MemberGoal, MemberGoalSnapshot> {
    private final LocalDate date;

    @Override
    public MemberGoalSnapshot process(MemberGoal item) {
        return new MemberGoalSnapshot(item.getMemberId(), item.getGoal(), false, date);
    }
}
