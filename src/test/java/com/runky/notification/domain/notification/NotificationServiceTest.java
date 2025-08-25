package com.runky.notification.domain.notification;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.notification.domain.aggregate.PushCommand;
import com.runky.notification.domain.aggregate.PushService;
import com.runky.utils.DatabaseCleanUp;

@SpringBootTest
class NotificationServiceTest {

	@Autowired
	NotificationService notificationService;

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	PushService pushService;

	@Autowired
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	@DisplayName("20건 발송 후 getRecentTopN(10)은 최신 10건을 반환한다(내림차순).")
	@Test
	void push_20_then_recentTop10_desc() {
		// given
		Long receiverId = 2L;
		Long senderId = 10L;

		pushService.registerDeviceToken(new PushCommand.DeviceToken.Register(receiverId, "token-xyz", "IOS"));

		for (int i = 1; i <= 20; i++) {
			var cmd = new PushCommand.Notify.ToOne(
				senderId,
				receiverId,
				new NotificationMessage.Cheer(new Nickname("닉네임" + i)),
				Map.of("no", String.valueOf(i))
			);
			pushService.pushToOne(cmd);
		}

		// when 최근 10건 조회
		NotificationInfo.Summaries summaries =
			notificationService.getRecentTopN(new NotificationCommand.GetRecentTopN(receiverId, 10));

		// then
		Assertions.assertThat(summaries.values()).hasSize(10);
		// createdAt 내림차순
		for (int i = 0; i < summaries.values().size() - 1; i++) {
			Assertions.assertThat(summaries.values().get(i).createdAt())
				.isAfterOrEqualTo(summaries.values().get(i + 1).createdAt());
		}
		// 가장 최근 건은 닉네임20
		var first = summaries.values().get(0);
		Assertions.assertThat(first.template()).isEqualTo(NotificationTemplate.CHEER);
		Assertions.assertThat(first.variables().get("NICKNAME")).isEqualTo("닉네임20");
		Assertions.assertThat(first.message()).isEqualTo("닉네임20님이 행운을 보냈어요!🍀");
	}
}
