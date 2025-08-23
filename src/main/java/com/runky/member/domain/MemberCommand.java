package com.runky.member.domain;

public class MemberCommand {

	public record RegisterFromExternal(String provider, String providerId, String nickname) {
	}

    public record Find(
            Long memberId
    ) {
    }
}
