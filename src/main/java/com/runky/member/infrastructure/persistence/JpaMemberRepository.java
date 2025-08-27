package com.runky.member.infrastructure.persistence;

import com.runky.member.domain.Member;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByExternalAccountProviderAndExternalAccountProviderId(String provider, String providerId);

    boolean existsByExternalAccountProviderAndExternalAccountProviderId(String provider, String providerId);

    List<Member> findByIdIn(Set<Long> ids);
}
