package com.runky.goal.batch;

import com.runky.reward.domain.CloverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@RequiredArgsConstructor
public class WeeklyMemberGoalWriter implements ItemWriter<MemberGoalAchieveInfo> {
    private final CloverRepository cloverRepository;

    @Override
    public void write(Chunk<? extends MemberGoalAchieveInfo> chunk) throws Exception {
        for (MemberGoalAchieveInfo info : chunk) {
            if (info.isAchieved()) {
                cloverRepository.addClover(info.memberId(), 1L);
            }
        }
    }
}
