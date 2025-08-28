package com.runky.member.api;

public class MemberResponse {

	private MemberResponse() {
	}

	public record Detail(
		Long userId,
		String nickname,
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
