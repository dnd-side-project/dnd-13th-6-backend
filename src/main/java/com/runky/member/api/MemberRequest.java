package com.runky.member.api;

public final class MemberRequest {

	private MemberRequest() {
	}

	public record Nickname(
		String nickname
	) {
	}

	public record Badge(
		Long badgeId
	) {
	}
}
