package com.runky.notification.domain.notification;

import com.runky.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
	name = "notifications",
	indexes = {
		@Index(name = "ix_notifications_receiver_id", columnList = "receiver_id"),
		@Index(name = "ix_notifications_receiver_created_at", columnList = "receiver_id,created_at")
	}
)
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Notification extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "sender_id", nullable = true)
	private Long senderId;

	@Column(name = "receiver_id", nullable = false)
	private Long receiverId;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String message;

	@Column(name = "is_read", nullable = false)
	private boolean isRead;

	public static Notification record(final Long senderId, final Long receiverId, final String title,
		final String message) {
		return Notification.builder()
			.senderId(senderId)
			.receiverId(receiverId)
			.title(title)
			.message(message)
			.isRead(false)
			.build();
	}

	public void markAsRead() {
		this.isRead = true;
	}
}
