package com.runky.goal.batch;

import com.runky.reward.domain.CloverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@RequiredArgsConstructor
public class CrewGoalSnapshotWriter implements ItemWriter<CrewGoalAchieveInfo> {
    private final CloverRepository cloverRepository;

    @Override
    public void write(Chunk<? extends CrewGoalAchieveInfo> chunk) throws Exception {
        for (CrewGoalAchieveInfo info : chunk) {
            cloverRepository.addCloverInCrew(info.crewId(), 3L);
        }
    }
}
