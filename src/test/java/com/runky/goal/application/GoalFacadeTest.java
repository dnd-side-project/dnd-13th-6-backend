package com.runky.goal.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.runky.crew.domain.Code;
import com.runky.crew.domain.Crew;
import com.runky.crew.domain.CrewCommand;
import com.runky.crew.domain.CrewRepository;
import com.runky.goal.application.GoalCriteria.LastWeekClover;
import com.runky.goal.domain.CrewGoalSnapshot;
import com.runky.goal.domain.GoalRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GoalFacadeTest {

    @Autowired
    private GoalFacade goalFacade;
    @Autowired
    private GoalRepository goalRepository;
    @Autowired
    private CrewRepository crewRepository;

    @Nested
    @DisplayName("저번 주 달성 크루 목표 클로버 개수 조회 시,")
    class getLastWeekCrewGoalClover {

        @Test
        @DisplayName("속해있는 크루 중, 목표를 달성한 크루 수에 맞는 클로버 수를 반환한다.")
        void returnClover() {
            Crew crew1 = crewRepository.save(Crew.of(new CrewCommand.Create(1L, "name1"), new Code("abc123")));
            Crew crew2 = crewRepository.save(Crew.of(new CrewCommand.Create(1L, "name2"), new Code("abc123")));
            Crew crew3 = crewRepository.save(Crew.of(new CrewCommand.Create(1L, "name3"), new Code("abc123")));
            CrewGoalSnapshot snapshot1 = CrewGoalSnapshot.empty(crew1.getId(), LocalDate.now().minusWeeks(1));
            snapshot1.achieve();
            goalRepository.save(snapshot1);
            CrewGoalSnapshot snapshot2 = CrewGoalSnapshot.empty(crew2.getId(), LocalDate.now().minusWeeks(1));
            snapshot2.achieve();
            goalRepository.save(snapshot2);
            CrewGoalSnapshot snapshot3 = CrewGoalSnapshot.empty(crew3.getId(), LocalDate.now().minusWeeks(1));
            goalRepository.save(snapshot3);

            CrewGoalSnapshotResult.Clover result = goalFacade.getLastWeekCrewGoalClover(new LastWeekClover(1L));

            assertThat(result.count()).isEqualTo(6);
        }
    }
}