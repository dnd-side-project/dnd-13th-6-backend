package com.runky.member.domain.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.runky.member.domain.ExternalAccount;
import com.runky.member.domain.Member;
import com.runky.member.domain.MemberCommand;
import com.runky.member.domain.dto.MemberInfo;
import com.runky.member.domain.MemberRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberRegistrar {
	private final MemberRepository memberRepository;

	public MemberInfo.Summary registerFromExternal(MemberCommand.RegisterFromExternal command) {
		ExternalAccount account = ExternalAccount.of(command.provider(), command.providerId());
		String nickname = command.nickname();

		Optional<Member> existing = memberRepository
			.findByExternalAccountProviderAndExternalAccountProviderId(account.provider(), account.providerId());
		if (existing.isPresent()) {
			Member m = existing.get();
			return new MemberInfo.Summary(m.getId(), m.getRole(), m.getNickname().value());
		}

		Member member = Member.register(account, nickname);
		Member saved = memberRepository.save(member);
		return new MemberInfo.Summary(saved.getId(), saved.getRole(), saved.getNickname().value());

	}
}
