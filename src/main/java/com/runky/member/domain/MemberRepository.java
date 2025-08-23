package com.runky.member.domain;

import java.util.Optional;

public interface MemberRepository {
	boolean existsByExternalAccountProviderAndExternalAccountProviderId(String provider, String providerId);

	Optional<Member> findByExternalAccountProviderAndExternalAccountProviderId(String provider, String providerId);

	Member save(Member member);

    Optional<Member> findById(Long id);
}
