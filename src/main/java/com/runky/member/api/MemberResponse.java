package com.runky.member.api;

public class MemberResponse {

    public record Detail(
            Long id,
            String nickname,
            String badgeUrl
    ) {
    }

    public record Nickname(
            Long id,
            String nickname
    ) {
    }

    public record Badge(
            Long id,
            String badgeImageUrl
    ) {
    }

    private MemberResponse() {
    }
}
