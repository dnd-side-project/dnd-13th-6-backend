package com.runky.goal.batch;

import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.GoalRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@RequiredArgsConstructor
public class CrewGoalWriter implements ItemWriter<CrewGoalSnapshot> {
    private final GoalRepository goalRepository;

    @Override
    public void write(Chunk<? extends CrewGoalSnapshot> chunk) throws Exception {
        List<CrewGoalSnapshot> snapshots = new ArrayList<>(chunk.getItems());
        goalRepository.saveAllCrewGoalSnapshots(snapshots);
    }
}
