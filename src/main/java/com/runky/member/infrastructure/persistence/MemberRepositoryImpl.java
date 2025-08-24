package com.runky.member.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.runky.member.domain.Member;
import com.runky.member.domain.MemberRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
	private final JpaMemberRepository jpaMemberRepository;

	@Override
	public Optional<Member> findByExternalAccountProviderAndExternalAccountProviderId(String provider,
		String providerId) {
		return jpaMemberRepository.findByExternalAccountProviderAndExternalAccountProviderId(provider, providerId);
	}

	@Override
	public boolean existsByExternalAccountProviderAndExternalAccountProviderId(String provider, String providerId) {
		return jpaMemberRepository.existsByExternalAccountProviderAndExternalAccountProviderId(provider, providerId);
	}

	@Override
	public Member save(Member member) {
		return jpaMemberRepository.save(member);
	}

    @Override
    public Optional<Member> findById(Long id) {
        return jpaMemberRepository.findById(id);
    }
}
