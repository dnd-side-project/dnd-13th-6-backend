package com.runky.notification.domain.push;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.notification.domain.notification.Notification;
import com.runky.notification.domain.notification.NotificationRepository;

@SpringBootTest
class PushServiceTest {

	@Autowired
	PushService pushService;

	@Autowired
	NotificationRepository notificationRepository;

	@DisplayName("알림 발송 시, 알림 엔티티가 영속화 된다.")
	@Test
	void when_message_push_then_notification_save() {
		// given
		Long receiverId = 1L;
		Long senderId = 10L;

		var command1 = new PushCommand.DeviceToken.Register(receiverId, "12345", "IOS");
		pushService.registerDeviceToken(command1);

		// when
		var command2 = new PushCommand.Push.ToOne(senderId, receiverId, "제목", "바디", null);
		pushService.pushToOne(command2);

		// then
		Optional<Notification> byReceiverId = notificationRepository.findByReceiverId(receiverId);
		Assertions.assertThat(byReceiverId).isNotNull();

		Notification notification = byReceiverId.get();
		Assertions.assertThat(notification.getReceiverId()).isEqualTo(receiverId);
		Assertions.assertThat(notification.getSenderId()).isEqualTo(senderId);
	}

}
