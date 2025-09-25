package com.runky.notification.interfaces.consumer;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.runky.notification.application.PushCommandHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

	private final PushCommandHandler handler;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void pushToOne(NotificationEvent.NotifyToOne command) {
		handler.pushToOne(command);
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void pushToMany(NotificationEvent.NotifyToMany command) {
		handler.pushToMany(command);
	}
}
