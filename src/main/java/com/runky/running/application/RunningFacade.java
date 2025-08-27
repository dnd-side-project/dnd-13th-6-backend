package com.runky.running.application;

import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.crew.domain.CrewService;
import com.runky.member.domain.MemberCommand;
import com.runky.member.domain.MemberService;
import com.runky.notification.domain.aggregate.PushCommand;
import com.runky.notification.domain.aggregate.PushService;
import com.runky.notification.domain.notification.Nickname;
import com.runky.notification.domain.notification.NotificationMessage;
import com.runky.running.domain.RunningCommand;
import com.runky.running.domain.RunningInfo;
import com.runky.running.domain.RunningService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RunningFacade {
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private final RunningService runningService;
	private final PushService pushService;
	private final MemberService memberService;
	private final CrewService crewService;

	@Transactional
	public RunningResult.Start start(RunningCriteria.Start criteria) {
		RunningInfo.Start info = runningService.start(criteria.toCommand());
		List<Long> receiverIds = crewService.getAllCrewMembersOfUser(criteria.runnerId());

		if (receiverIds.isEmpty()) {
			return RunningResult.Start.from(info);
		}

		var memberFindCmd = new MemberCommand.Find(criteria.runnerId());
		String runnerNickname = memberService.getMember(memberFindCmd).getNickname().value();

		var pushToManyCmd = new PushCommand.Notify.ToMany(
			criteria.runnerId(), receiverIds,
			new NotificationMessage.RunStarted(new Nickname(runnerNickname)),
			null
		);
		pushService.pushToMany(pushToManyCmd);

		return RunningResult.Start.from(info);
	}

	@Transactional
	public RunningResult.End end(RunningCriteria.End criteria) {
		RunningInfo.End info = runningService.end(criteria.toCommand());
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
}
