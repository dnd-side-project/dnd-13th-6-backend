package com.runky.cheer.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.cheer.domain.CheerCommand;
import com.runky.cheer.domain.CheerInfo;
import com.runky.cheer.domain.CheerService;
import com.runky.global.error.GlobalException;
import com.runky.member.infrastructure.persistence.JpaMemberRepository;
import com.runky.notification.domain.notification.Nickname;
import com.runky.notification.domain.notification.NotificationMessage;
import com.runky.notification.interfaces.consumer.NotificationEvent;
import com.runky.running.domain.RunningService;
import com.runky.running.error.RunningErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheerFacade {

	private final RunningService runningService;
	private final CheerService cheerService;
	private final JpaMemberRepository memberRepository;
	private final ApplicationEventPublisher eventPublisher;

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

		eventPublisher.publishEvent(new NotificationEvent.NotifyToOne(
			criteria.senderId(), criteria.receiverId(), new NotificationMessage.Cheer(new Nickname(nickname))
		));

		return new CheerResult.Sent(
			sentInfo.cheerId(), sentInfo.runningId(), criteria.senderId(), sentInfo.receiverId(),
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

}
