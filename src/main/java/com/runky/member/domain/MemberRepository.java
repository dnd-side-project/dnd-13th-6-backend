package com.runky.member.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MemberRepository {
	boolean existsByExternalAccountProviderAndExternalAccountProviderId(String provider, String providerId);

	Optional<Member> findByExternalAccountProviderAndExternalAccountProviderId(String provider, String providerId);

	Member save(Member member);

	Optional<Member> findById(Long id);

	List<Member> findMembers(Set<Long> ids);

	void delete(Long id);
}
