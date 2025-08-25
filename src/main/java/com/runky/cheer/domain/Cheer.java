package com.runky.cheer.domain;

import org.springframework.util.StringUtils;

import com.runky.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cheers",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "ux_cheers_running_sender_receiver",
			columnNames = {"running_id", "sender_id", "receiver_id"}
		)
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Cheer extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "running_id", nullable = false)
	private Long runningId;

	@Column(name = "sender_id", nullable = false)
	private Long senderId;

	@Column(name = "receiver_id", nullable = false)
	private Long receiverId;

	@Column(nullable = false, length = 120)
	private String message;

	public static Cheer of(Long runningId, Long senderId, Long receiverId, String message) {

		if (senderId.equals(receiverId))
			throw new IllegalArgumentException("[ERROR] 자신에게는 응원을 보낼 수 없습니다.");
		if (!StringUtils.hasText(message))
			throw new IllegalArgumentException("[ERROR] 응원 메시지는 필수입니다.");

		return Cheer.builder()
			.runningId(runningId)
			.senderId(senderId)
			.receiverId(receiverId)
			.message(message)
			.build();
	}
}
