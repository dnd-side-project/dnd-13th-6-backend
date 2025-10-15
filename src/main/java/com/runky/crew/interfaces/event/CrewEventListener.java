package com.runky.crew.interfaces.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.runky.auth.application.AuthEvent;
import com.runky.crew.domain.CrewCommand;
import com.runky.crew.domain.CrewService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CrewEventListener {
	private final CrewService crewService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void consume(AuthEvent.SignupCompleted event) {
		crewService.init(new CrewCommand.Init(event.memberId()));
	}
}
