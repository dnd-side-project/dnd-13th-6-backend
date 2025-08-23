package com.runky.notification.domain.notification;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationServiceTest {

	@Autowired
	NotificationService notificationService;

	@Autowired
	NotificationRepository notificationRepository;

	@DisplayName("최신순(생성일 내림차순, id 내림차순)으로 최대 10개의 알림을 조회한다.")
	@Test
	void recentTop10_returns_latest_first() throws Exception {
		// given
		Long receiverId = 1L;

		// senderId 1..20을 저장 (최신이 뒤에서 들어오게함)
		for (long senderId = 1; senderId <= 20; senderId++) {
			notificationRepository.save(
				Notification.record(senderId, receiverId, "제목" + senderId, "메세지" + senderId)
			);

		}

		NotificationCommand.GetRecentTopN command =
			new NotificationCommand.GetRecentTopN(receiverId, 10);

		// when
		NotificationInfo.Summaries summaries = notificationService.getRecentTopN(command);
		List<NotificationInfo.Summary> values = summaries.values();

		// then
		assertThat(values).hasSize(10);

		// 최신순으로 20..11의 senderId가 와야 한다.
		assertThat(values)
			.extracting(NotificationInfo.Summary::senderId)
			.containsExactly(20L, 19L, 18L, 17L, 16L, 15L, 14L, 13L, 12L, 11L);

		assertThat(values)
			.extracting(NotificationInfo.Summary::title)
			.containsExactly("제목20", "제목19", "제목18", "제목17", "제목16",
				"제목15", "제목14", "제목13", "제목12", "제목11");
	}
}
