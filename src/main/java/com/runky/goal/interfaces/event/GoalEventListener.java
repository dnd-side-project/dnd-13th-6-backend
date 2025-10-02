package com.runky.goal.interfaces.event;

import com.runky.auth.application.AuthEvent;
import com.runky.goal.application.GoalCriteria;
import com.runky.goal.application.GoalFacade;
import com.runky.goal.domain.GoalCommand;
import com.runky.goal.domain.GoalService;
import com.runky.running.application.RunningEvent;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class GoalEventListener {
    private final GoalFacade goalFacade;
    private final GoalService goalService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void consume(AuthEvent.SignupCompleted event) {
        goalService.init(new GoalCommand.Init(event.memberId()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(RunningEvent.Ended event) {
        goalFacade.updateSnapshots(new GoalCriteria.UpdateDistance(event.runnerId(), BigDecimal.valueOf(event.distance())));
    }
}
