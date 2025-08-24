package com.runky.notification.domain.notification;

import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.runky.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	@Enumerated(EnumType.STRING)
	@Column(name = "template_code", length = 64, nullable = false)
	private NotificationTemplate template;

	/** JSON: {"CREW_NAME":"완주GO","NICKNAME":"인생한접시"} */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "variables", columnDefinition = "json", nullable = false)
	private Map<String, String> variables;

	public static Notification of(
		Long senderId,
		Long receiverId,
		NotificationTemplate template,
		Map<NotificationTemplate.VarKey, String> vars
	) {
		String text = template.renderText(vars);
		Map<String, String> stored = vars.entrySet().stream()
			.collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

		return Notification.builder()
			.senderId(senderId)
			.receiverId(receiverId)
			.title(template.title())
			.message(text)
			.isRead(false)
			.template(template)
			.variables(stored)
			.build();
	}

	public void markAsRead() {
		this.isRead = true;
	}
}
