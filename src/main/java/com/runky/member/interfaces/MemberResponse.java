package com.runky.member.interfaces;

public final class MemberResponse {

	private MemberResponse() {
	}

	public record Detail(
		Long userId,
		String nickname,
		Long badgeId,
		String badgeUrl
	) {
	}

	public record Nickname(
		Long userId,
		String nickname
	) {
	}

	public record Badge(
		Long userId,
		Long badgeId,
		String badgeImageUrl
	) {
	}
}
