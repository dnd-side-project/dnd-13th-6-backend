package com.runky.member.application;

import com.runky.member.domain.Member;
import com.runky.reward.domain.Badge;

public record MemberResult(
	Long id,
	String role,
	String nickname
) {
	public static MemberResult from(Member member) {
		return new MemberResult(
			member.getId(),
			member.getRole().name(),
			member.getNickname().value()
		);
	}

	public record WithBadge(
		Long id,
		String role,
		String nickname,
		Long badgeId,
		String badgeImageUrl
	) {
		public static MemberResult.WithBadge of(Member member, Badge badge) {
			return new MemberResult.WithBadge(
				member.getId(),
				member.getRole().name(),
				member.getNickname().value(),
				badge.getId(),
				badge.getImageUrl()
			);
		}
	}
}
