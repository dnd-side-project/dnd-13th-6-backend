package com.runky.notification.domain.notification;

import java.util.List;

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
	public void recordsByTemplate(NotificationCommand.RecordByTemplate command) {
		Notification notification = Notification.of(
			command.senderId(), command.receiverId(), command.template(), command.variables());
		notificationRepository.save(notification);
	}

	//TODO: 배치 저장
	@Transactional
	public void recordsByTemplate(NotificationCommand.RecordsByTemplate command) {

		List<Notification> list = command.receiverIds().stream()
			.map(receiverId -> Notification.of(command.senderId(), receiverId, command.template(), command.variables()))
			.toList();
		notificationRepository.saveAll(list);
	}

	@Transactional(readOnly = true)
	public NotificationInfo.Summaries getRecentTopN(NotificationCommand.GetRecentTopN command) {
		PageRequest pageRequest = PageRequest.of(0, command.limit(), Sort.by(Sort.Direction.DESC, "createdAt"));

		Page<Notification> notificationPage = notificationRepository.findByReceiverId(command.receiverId(),
			pageRequest);

		var values = notificationPage.getContent().stream()
			.map(n -> new NotificationInfo.Summary(
				n.getId(), n.getTitle(), n.getMessage(), n.getSenderId(), n.isRead(), n.getCreatedAt().toInstant(),
				n.getTemplate(), n.getVariables()
			))
			.toList();
		return new NotificationInfo.Summaries(values);
	}
}
