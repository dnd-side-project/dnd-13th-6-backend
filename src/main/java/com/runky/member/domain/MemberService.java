package com.runky.member.domain;

import com.runky.global.error.GlobalException;
import com.runky.member.error.MemberErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member getMember(MemberCommand.Find command) {
        return memberRepository.findById(command.memberId())
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public Member changeNickname(MemberCommand.ChangeNickname command) {
        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
        member.changeNickname(command.nickname());
        try {
            memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            throw new GlobalException(MemberErrorCode.DUPLICATE_NICKNAME);
        }
        return member;
    }

    @Transactional
    public Member changeBadge(MemberCommand.ChangeBadge command) {
        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(() -> new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
        member.changeBadge(command.badgeId());
        return member;
    }

    @Transactional(readOnly = true)
    public List<Member> getMembers(MemberCommand.GetMembers command) {
        return memberRepository.findMembers(command.memberIds());
    }
}
