package com.runky.reward.interfaces.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.runky.auth.application.AuthEvent;
import com.runky.reward.domain.RewardCommand;
import com.runky.reward.domain.RewardService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RewardEventListener {
	private final RewardService rewardService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(AuthEvent.SignupCompleted event) {
		rewardService.init(new RewardCommand.Init(event.memberId()));
	}
}
