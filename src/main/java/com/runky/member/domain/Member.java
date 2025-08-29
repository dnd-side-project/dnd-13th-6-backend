package com.runky.member.domain;

import com.runky.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "member",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_member_provider_provider_id",
		columnNames = {"provider", "provider_id"}
	),
	indexes = @Index(name = "ix_member_provider_provider_id", columnList = "provider,provider_id")
)
public class Member extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	private ExternalAccount externalAccount;

	@Enumerated(EnumType.STRING)
	private MemberRole role;

	@Embedded
	private Nickname nickname;

	@Column(name = "badge_id", nullable = false)
	private Long badgeId;

	@Builder(access = AccessLevel.PRIVATE)
	private Member(Long id, ExternalAccount externalAccount, MemberRole role, Nickname nickname, Long badgeId) {
		this.id = id;
		this.externalAccount = externalAccount;
		this.role = role;
		this.nickname = nickname;
		this.badgeId = badgeId;
	}

	public static Member register(ExternalAccount account, String nickname) {
		return Member.builder()
			.externalAccount(account)
			.role(MemberRole.USER)
			.nickname(new Nickname(nickname))
			.badgeId(1L)
			.build();
	}

	public void changeBadge(Long badgeId) {
		this.badgeId = badgeId;
	}

	public void changeNickname(String nickname) {
		this.nickname = new Nickname(nickname);
	}
}
