package com.runky.member.domain;

import com.runky.global.error.GlobalException;
import com.runky.member.error.MemberErrorCode;
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
}
