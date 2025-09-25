package com.runky.goal.batch;

import com.runky.goal.domain.GoalRepository;
import com.runky.goal.domain.MemberGoalSnapshot;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@RequiredArgsConstructor
public class MemberGoalWriter implements ItemWriter<MemberGoalSnapshot> {
    private final GoalRepository goalRepository;

    @Override
    public void write(Chunk<? extends MemberGoalSnapshot> chunk) throws Exception {
        List<MemberGoalSnapshot> snapshots = new ArrayList<>(chunk.getItems());
        goalRepository.saveAll(snapshots);
    }
}
