package com.runky.goal.domain.batch;

import com.runky.goal.domain.WeekUnit;
import com.runky.reward.domain.CloverRepository;
import com.runky.reward.domain.MemberCloverHistory;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@RequiredArgsConstructor
public class WeeklyMemberGoalWriter implements ItemWriter<MemberGoalAchieveInfo> {
    private final CloverRepository cloverRepository;
    private final LocalDate date;

    @Override
    public void write(Chunk<? extends MemberGoalAchieveInfo> chunk) throws Exception {
        WeekUnit lastWeek = WeekUnit.from(date.minusWeeks(1));
        for (MemberGoalAchieveInfo info : chunk) {
            Optional<MemberCloverHistory> history = cloverRepository.findMemberCloverHistory(info.memberId(), lastWeek);
            if (info.isAchieved() && history.isEmpty()) {
                cloverRepository.addClover(info.memberId(), 1L);
                cloverRepository.save(new MemberCloverHistory(info.memberId(), lastWeek, 1L));
            }
        }
    }
}
