package com.runky.notification.domain.notification;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationRepository notificationRepository;

	@Transactional
	public void record(NotificationCommand.Record command) {
		Notification notification = Notification.record(command.senderId(), command.receiverId(), command.title(),
			command.message());
		notificationRepository.save(notification);
	}

	//TODO: 배치 저장
	@Transactional
	public void records(NotificationCommand.Records command) {
		List<Notification> notifications = command.receiverIds().stream()
			.map(receiverId -> Notification.record(command.senderId(), receiverId, command.title(), command.message()))
			.collect(Collectors.toList());
		notificationRepository.saveAll(notifications);
	}
}
