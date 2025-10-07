package com.runky.goal.domain.batch;

import com.runky.goal.domain.WeekUnit;
import com.runky.reward.domain.CloverRepository;
import com.runky.reward.domain.CrewCloverHistory;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@RequiredArgsConstructor
public class WeeklyCrewGoalSnapshotWriter implements ItemWriter<CrewGoalAchieveInfo> {
    private final CloverRepository cloverRepository;
    private final LocalDate date;

    @Override
    public void write(Chunk<? extends CrewGoalAchieveInfo> chunk) throws Exception {
        WeekUnit lastWeek = WeekUnit.from(date.minusWeeks(1));
        for (CrewGoalAchieveInfo info : chunk) {
            Optional<CrewCloverHistory> history = cloverRepository.findCrewCloverHistory(info.crewId(), lastWeek);
            if (info.isAchieved() && history.isEmpty()) {
                cloverRepository.addCloverInCrew(info.crewId(), 3L);
                cloverRepository.save(new CrewCloverHistory(info.crewId(), lastWeek, 3L));
            }
        }
    }
}
