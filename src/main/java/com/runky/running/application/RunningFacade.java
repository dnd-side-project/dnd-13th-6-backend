package com.runky.running.application;

import java.time.ZoneId;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.crew.domain.CrewService;
import com.runky.member.domain.MemberCommand;
import com.runky.member.domain.MemberService;
import com.runky.notification.domain.notification.Nickname;
import com.runky.notification.domain.notification.NotificationMessage;
import com.runky.notification.interfaces.consumer.NotificationEvent;
import com.runky.running.domain.RunningCommand;
import com.runky.running.domain.RunningInfo;
import com.runky.running.domain.RunningService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RunningFacade {
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private final RunningService runningService;
	private final MemberService memberService;
	private final CrewService crewService;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public RunningResult.Start start(RunningCriteria.Start criteria) {
		RunningInfo.Start info = runningService.start(criteria.toCommand());
		List<Long> receiverIds = crewService.getAllCrewMembersOfUser(criteria.runnerId());

		if (receiverIds.isEmpty()) {
			return RunningResult.Start.from(info);
		}

		var memberFindCmd = new MemberCommand.Find(criteria.runnerId());
		String runnerNickname = memberService.getMember(memberFindCmd).getNickname().value();

		var pushToManyCmd = new NotificationEvent.NotifyToMany(
			criteria.runnerId(), receiverIds,
			new NotificationMessage.RunStarted(new Nickname(runnerNickname)));
		eventPublisher.publishEvent(pushToManyCmd);
		return RunningResult.Start.from(info);
	}

	@Transactional
	public RunningResult.End end(RunningCriteria.End criteria) {
		RunningInfo.End info = runningService.end(criteria.toCommand());
        eventPublisher.publishEvent(new RunningEvent.Ended(info.runningId(), info.runnerId(), info.distance(),
                info.status(), info.startedAt(), info.endedAt()));
		return RunningResult.End.from(info);
	}

	@Transactional(readOnly = true)
	public RunningResult.TodaySummary getTodaySummary(RunningCriteria.TodaySummary criteria) {

		return RunningResult.TodaySummary.from(runningService.getTodaySummary(criteria.runnerId(), criteria.now()));
	}

	@Transactional(readOnly = true)
	public RunningResult.MyWeeklyTotalDistance getMyWeeklyTotalDistance(
		RunningCriteria.MyWeeklyTotalDistance criteria
	) {
		RunningInfo.MyWeek info = runningService.getMyWeeklyTotalDistance(
			new RunningCommand.MyWeeklyTotalDistance(criteria.runnerId())
		);
		return RunningResult.MyWeeklyTotalDistance.from(info);
	}

	@Transactional(readOnly = true)
	public RunningResult.RunResult getRunResult(RunningCriteria.RunResult criteria) {
		var info = runningService.getRunResult(
			new RunningCommand.RunResult(criteria.runningId(), criteria.runningId()));
		return RunningResult.RunResult.from(info);
	}

	public RunningResult.RemovedRunning removeActiveRunning(RunningCriteria.RemoveActiveRunning criteria) {
		int count = runningService.removeActiveRunning(
			new RunningCommand.RemoveActiveRunning(criteria.runnerId(), criteria.runningId())
		);
		return new RunningResult.RemovedRunning(count);
	}
}
