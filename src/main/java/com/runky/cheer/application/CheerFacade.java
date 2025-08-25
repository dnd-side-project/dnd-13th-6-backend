package com.runky.cheer.application;

import static java.util.Map.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.runky.cheer.domain.CheerCommand;
import com.runky.cheer.domain.CheerInfo;
import com.runky.cheer.domain.CheerService;
import com.runky.crew.domain.CrewService;
import com.runky.global.error.GlobalException;
import com.runky.member.infrastructure.persistence.JpaMemberRepository;
import com.runky.notification.domain.push.PushCommand;
import com.runky.notification.domain.push.PushService;
import com.runky.running.domain.RunningService;
import com.runky.running.error.RunningErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheerFacade {

	private final RunningService runningService;
	private final CrewService crewService;
	private final PushService pushService;
	private final CheerService cheerService;
	private final JpaMemberRepository memberRepository;

	@Transactional
	public CheerResult.Sent send(CheerCriteria.Send criteria) {

		// 런닝 상태에서만 응원 보내기 가능
		validateIsRunning(criteria);

		// 런닝Id 대상이 해당 러닝의 러너여야함
		validateRunnerOwnRunning(criteria);

		//		// 응원을 보내려는 런너가 나와 같은 크루여야함 미구현
		//		if (!crewService.inSameCrew(criteria.senderId(), criteria.receiverId())) {
		//			throw new GlobalException(CrewErrorCode.NOT_CREW_MEMBER);
		//		}

		String nickname = memberRepository.findById(criteria.senderId()).get().getNickname().value();

		CheerInfo.Detail sentInfo = cheerService.create(
			new CheerCommand.Create(criteria.runningId(), criteria.senderId(), criteria.receiverId(),
				criteria.message()));

		pushService.pushToOne(new PushCommand.Push.ToOne(
			criteria.senderId(), criteria.receiverId(),
			"응원 도착!", criteria.message(),
			of("type", "CHEER", "runningId", String.valueOf(criteria.runningId()))
		));
		String message = nickname + "님이 응원을 보내셨어요!";

		return new CheerResult.Sent(
			sentInfo.cheerId(), sentInfo.runningId(), criteria.senderId(), sentInfo.receiverId(), message,
			sentInfo.sentAt()
		);
	}

	private void validateRunnerOwnRunning(final CheerCriteria.Send criteria) {
		Long runnerId = runningService.getRunnerId(criteria.runningId());
		if (!runnerId.equals(criteria.receiverId())) {
			throw new GlobalException(RunningErrorCode.NOT_FOUND_RUNNING);
		}
	}

	private void validateIsRunning(final CheerCriteria.Send criteria) {
		if (!runningService.isActive(criteria.runningId())) {
			throw new GlobalException(RunningErrorCode.NOT_ACTIVE_RUNNING);
		}
	}

	private void afterCommit(Runnable task) {
		if (TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					task.run();
				}
			});
		} else {
			task.run();
		}
	}
}
