package com.runky.member.interfaces;

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
