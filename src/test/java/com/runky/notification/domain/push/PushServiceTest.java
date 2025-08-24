package com.runky.notification.domain.push;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.notification.domain.aggregate.PushCommand;
import com.runky.notification.domain.aggregate.PushInfo;
import com.runky.notification.domain.aggregate.PushService;
import com.runky.notification.domain.notification.CrewName;
import com.runky.notification.domain.notification.Nickname;
import com.runky.notification.domain.notification.NotificationCommand;
import com.runky.notification.domain.notification.NotificationInfo;
import com.runky.notification.domain.notification.NotificationMessage;
import com.runky.notification.domain.notification.NotificationRepository;
import com.runky.notification.domain.notification.NotificationService;
import com.runky.notification.domain.notification.NotificationTemplate;
import com.runky.utils.DatabaseCleanUp;

@SpringBootTest
class PushServiceTest {

	@Autowired
	PushService pushService;
	@Autowired
	NotificationService notificationService;

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	@DisplayName("CHEER: 닉네임만으로 발송/저장/조회가 정상 동작한다.")
	@Test
	void cheer_one_ok() {
		// given
		Long receiverId = 101L;
		Long senderId = 10L;

		pushService.registerDeviceToken(new PushCommand.DeviceToken.Register(receiverId, "token-cheer", "IOS"));

		var cmd = new PushCommand.Notify.ToOne(
			senderId,
			receiverId,
			new NotificationMessage.Cheer(new Nickname("인생한접시")),
			Map.of("meta", "x")
		);

		// when
		PushInfo.SenTPush.Summary summary = pushService.pushToOne(cmd);
		NotificationInfo.Summaries result =
			notificationService.getRecentTopN(new NotificationCommand.GetRecentTopN(receiverId, 10));

		// then
		Assertions.assertThat(summary.success()).isEqualTo(1);
		Assertions.assertThat(result.values()).hasSize(1);
		var n = result.values().get(0);
		Assertions.assertThat(n.template()).isEqualTo(NotificationTemplate.CHEER);
		Assertions.assertThat(n.title()).isEqualTo("응원");
		Assertions.assertThat(n.message()).isEqualTo("인생한접시님이 응원을 보내셨어요!");
		Assertions.assertThat(n.variables().get("NICKNAME")).isEqualTo("인생한접시");
	}

	@DisplayName("GOAL_WEEKLY_ACHIEVED: 변수 없이 발송/저장/조회가 정상 동작한다.")
	@Test
	void goal_weekly_achieved_one_ok() {
		// given
		Long receiverId = 102L;
		Long senderId = 10L;

		pushService.registerDeviceToken(new PushCommand.DeviceToken.Register(receiverId, "token-goal", "IOS"));

		var cmd = new PushCommand.Notify.ToOne(
			senderId,
			receiverId,
			new NotificationMessage.GoalWeeklyAchieved(),
			Map.of()
		);

		// when
		pushService.pushToOne(cmd);
		var result = notificationService.getRecentTopN(new NotificationCommand.GetRecentTopN(receiverId, 10));

		// then
		Assertions.assertThat(result.values()).hasSize(1);
		var n = result.values().get(0);
		Assertions.assertThat(n.template()).isEqualTo(NotificationTemplate.GOAL_WEEKLY_ACHIEVED);
		Assertions.assertThat(n.title()).isEqualTo("목표 달성");
		Assertions.assertThat(n.message()).isEqualTo("우리 크루, 이번 주도 완주 GO! 크루가 이번 주 목표를 달성 했어요!");
		Assertions.assertThat(n.variables()).isEmpty();
	}

	@DisplayName("CREW_NEW_MEMBER: 닉네임을 포함해 발송/저장/조회가 정상 동작한다.")
	@Test
	void crew_new_member_one_ok() {
		// given
		Long receiverId = 103L;
		Long senderId = 10L;

		pushService.registerDeviceToken(new PushCommand.DeviceToken.Register(receiverId, "token-member", "IOS"));

		var cmd = new PushCommand.Notify.ToOne(
			senderId,
			receiverId,
			new NotificationMessage.CrewNewMember(new Nickname("새멤버")),
			Map.of()
		);

		// when
		pushService.pushToOne(cmd);
		var result = notificationService.getRecentTopN(new NotificationCommand.GetRecentTopN(receiverId, 10));

		// then
		Assertions.assertThat(result.values()).hasSize(1);
		var n = result.values().get(0);
		Assertions.assertThat(n.template()).isEqualTo(NotificationTemplate.CREW_NEW_MEMBER);
		Assertions.assertThat(n.title()).isEqualTo("크루");
		Assertions.assertThat(n.message()).isEqualTo("우리 크루, 이번 주도 완주 GO! 크루에 새 멤버 새멤버님이 들어왔어요.");
		Assertions.assertThat(n.variables().get("NICKNAME")).isEqualTo("새멤버");
	}

	@DisplayName("CREW_NEW_LEADER: 닉네임을 포함해 발송/저장/조회가 정상 동작한다.")
	@Test
	void crew_new_leader_one_ok() {
		// given
		Long receiverId = 104L;
		Long senderId = 10L;

		pushService.registerDeviceToken(new PushCommand.DeviceToken.Register(receiverId, "token-leader", "IOS"));

		var cmd = new PushCommand.Notify.ToOne(
			senderId,
			receiverId,
			new NotificationMessage.CrewNewLeader(new Nickname("태훈")),
			Map.of()
		);

		// when
		pushService.pushToOne(cmd);
		var result = notificationService.getRecentTopN(new NotificationCommand.GetRecentTopN(receiverId, 10));

		// then
		Assertions.assertThat(result.values()).hasSize(1);
		var n = result.values().get(0);
		Assertions.assertThat(n.template()).isEqualTo(NotificationTemplate.CREW_NEW_LEADER);
		Assertions.assertThat(n.title()).isEqualTo("크루");
		Assertions.assertThat(n.message()).isEqualTo("태훈 님이 새로운 크루 리더가 되었어요.");
		Assertions.assertThat(n.variables().get("NICKNAME")).isEqualTo("태훈");
	}

	@DisplayName("CREW_DISBANDED: 크루명을 포함해 발송/저장/조회가 정상 동작한다.")
	@Test
	void crew_disbanded_one_ok() {
		// given
		Long receiverId = 105L;
		Long senderId = 10L;

		pushService.registerDeviceToken(new PushCommand.DeviceToken.Register(receiverId, "token-disband", "IOS"));

		var cmd = new PushCommand.Notify.ToOne(
			senderId,
			receiverId,
			new NotificationMessage.CrewDisbanded(new CrewName("완주GO")),
			Map.of()
		);

		// when
		pushService.pushToOne(cmd);
		var result = notificationService.getRecentTopN(new NotificationCommand.GetRecentTopN(receiverId, 10));

		// then
		Assertions.assertThat(result.values()).hasSize(1);
		var n = result.values().get(0);
		Assertions.assertThat(n.template()).isEqualTo(NotificationTemplate.CREW_DISBANDED);
		Assertions.assertThat(n.title()).isEqualTo("크루");
		Assertions.assertThat(n.message()).isEqualTo("완주GO 크루가 크루 리더에 의해 해체되었어요.");
		Assertions.assertThat(n.variables().get("CREW_NAME")).isEqualTo("완주GO");
	}

	@DisplayName("RUN_STARTED: 크루명+닉네임으로 발송/저장/조회가 정상 동작한다.")
	@Test
	void run_started_one_ok() {
		// given
		Long receiverId = 106L;
		Long senderId = 10L;

		pushService.registerDeviceToken(new PushCommand.DeviceToken.Register(receiverId, "token-run", "IOS"));

		var cmd = new PushCommand.Notify.ToOne(
			senderId,
			receiverId,
			new NotificationMessage.RunStarted(new CrewName("완주GO"), new Nickname("진수")),
			Map.of("crewId", "123")
		);

		// when
		pushService.pushToOne(cmd);
		var result = notificationService.getRecentTopN(new NotificationCommand.GetRecentTopN(receiverId, 10));

		// then
		Assertions.assertThat(result.values()).hasSize(1);
		var n = result.values().get(0);
		Assertions.assertThat(n.template()).isEqualTo(NotificationTemplate.RUN_STARTED);
		Assertions.assertThat(n.title()).isEqualTo("런닝");
		Assertions.assertThat(n.message()).isEqualTo("완주GO의 진수님이 런닝을 시작했어요!");
		Assertions.assertThat(n.variables().get("CREW_NAME")).isEqualTo("완주GO");
		Assertions.assertThat(n.variables().get("NICKNAME")).isEqualTo("진수");
	}

}
