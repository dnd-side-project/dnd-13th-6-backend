package com.runky.member.domain.service;

import org.springframework.stereotype.Component;

import com.runky.member.domain.Member;
import com.runky.member.domain.dto.MemberInfo;
import com.runky.member.error.MemberNotFoundException;
import com.runky.member.domain.MemberRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberReader {
	private final MemberRepository memberRepository;

	public boolean existsByExternalAccount(String provider, String providerId) {
		return memberRepository.existsByExternalAccountProviderAndExternalAccountProviderId(provider, providerId);
	}

	public MemberInfo.Summary getInfoByExternalAccount(String provider, String providerId) {
		Member member = memberRepository.findByExternalAccountProviderAndExternalAccountProviderId(provider, providerId)
			.orElseThrow(MemberNotFoundException::new);

		return new MemberInfo.Summary(member.getId(), member.getRole(), member.getNickname().value());
	}
}
