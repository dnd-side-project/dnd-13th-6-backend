package com.runky.goal.interfaces.event;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.runky.auth.application.AuthEvent;
import com.runky.goal.application.GoalCriteria;
import com.runky.goal.application.GoalFacade;
import com.runky.running.application.RunningEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoalEventListener {
	private final GoalFacade goalFacade;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void consume(AuthEvent.SignupCompleted event) {
		goalFacade.init(new GoalCriteria.Init(event.memberId()));
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(RunningEvent.Ended event) {
		BigDecimal km = BigDecimal.valueOf(event.distance()).divide(BigDecimal.valueOf(1000), 2, RoundingMode.FLOOR);
		goalFacade.updateSnapshots(
			new GoalCriteria.UpdateDistance(event.runnerId(), km, event.endedAt().toLocalDate()));
	}
}
