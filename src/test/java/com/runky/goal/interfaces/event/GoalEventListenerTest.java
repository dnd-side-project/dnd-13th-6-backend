package com.runky.goal.interfaces.event;

import com.runky.crew.domain.Code;
import com.runky.crew.domain.Crew;
import com.runky.crew.domain.CrewCommand;
import com.runky.crew.domain.CrewRepository;
import com.runky.goal.domain.*;
import com.runky.running.application.RunningEvent;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class GoalEventListenerTest {

    @Autowired
    private GoalRepository goalRepository;
    @Autowired
    private CrewRepository crewRepository;
    @MockitoSpyBean
    private GoalEventListener goalEventListener;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    @DisplayName("미터 단위의 거리를 킬로미터 단위로 변환하여 이번주 러닝 거리를 반영한다.")
    void updateSnapshotsWithKm() throws InterruptedException {
        MemberGoal memberGoal = MemberGoal.from(1L);
        memberGoal.updateGoal(new BigDecimal("10"));
        LocalDate now = LocalDate.of(2025, 10, 7);
        Crew crew = Crew.of(new CrewCommand.Create(1L, "crew"), new Code("ABC123"));
        crewRepository.save(crew);
        MemberGoalSnapshot memberSnapshot = goalRepository.save(memberGoal.createSnapshot(now));
        CrewGoalSnapshot crewGoalSnapshot = goalRepository.save(CrewGoalSnapshot.of(List.of(memberSnapshot), 1L, now));

        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        applicationEventPublisher.publishEvent(new RunningEvent.Ended(1L, 1L, 10000.0, "END", now.atStartOfDay(), now.atStartOfDay().plusMinutes(30)));
        transactionManager.commit(transaction);
        Awaitility.await()
                .atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> verify(goalEventListener, times(1)).handle(any()));

        MemberGoalSnapshot memberResult = goalRepository.findMemberGoalSnapshotOfWeek(1L, WeekUnit.from(now)).orElseThrow();
        CrewGoalSnapshot crewResult = goalRepository.findCrewGoalSnapshot(1L, WeekUnit.from(now)).orElseThrow();
        assertThat(memberResult.getRunDistance()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(memberResult.getAchieved()).isTrue();
        assertThat(crewResult.getRunDistance()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(crewResult.getAchieved()).isTrue();
    }
}