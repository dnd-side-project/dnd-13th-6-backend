package com.runky.notification.domain.push;

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

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "device_tokens", indexes = @Index(name = "idx_member_token", columnList = "member_id, token"))
public class DeviceToken extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "token", nullable = false, length = 512, unique = true)
	private String token;

	@Column(name = "device_type", nullable = false, unique = true)
	private String deviceType;

	@Column(name = "active", nullable = false)
	private boolean active;

	public static DeviceToken register(Long memberId, String token, String deviceType) {
		return DeviceToken.builder()
			.memberId(memberId)
			.token(token)
			.active(true)
			.deviceType(deviceType)
			.build();
	}

	public void reactivate() {
		this.active = true;
	}

	public void deactivate() {
		this.active = false;
	}
}
