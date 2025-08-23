package com.runky.notification.domain.notification;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

	@Transactional(readOnly = true)
	public NotificationInfo.Summaries getRecentTopN(NotificationCommand.GetRecentTopN command) {
		PageRequest pageRequest = PageRequest.of(0, command.limit(), Sort.by(Sort.Direction.DESC, "createdAt"));

		Page<Notification> notificationPage = notificationRepository.findByReceiverId(command.receiverId(),
			pageRequest);

		var values = notificationPage.getContent().stream()
			.map(n -> new NotificationInfo.Summary(
				n.getId(), n.getTitle(), n.getMessage(), n.getSenderId(), n.isRead(), n.getCreatedAt().toInstant()
			))
			.toList();
		return new NotificationInfo.Summaries(values);
	}
}
