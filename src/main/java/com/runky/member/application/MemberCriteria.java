package com.runky.member.application;

import com.runky.member.domain.MemberCommand;

public class MemberCriteria {

    public record Get(Long memberId) {
    }

    public record ChangeNickname(Long memberId, String nickname) {
        public MemberCommand.ChangeNickname toCommand() {
            return new MemberCommand.ChangeNickname(memberId, nickname);
        }
    }

    public record ChangeBadge(Long memberId, Long badgeId) {
    }
}
