package com.runky.notification.domain.notification;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	Optional<Notification> findByReceiverId(Long receiverId);

}
