package com.runky.notification.infrastructure.push;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.runky.notification.domain.push.DeviceToken;
import com.runky.notification.domain.push.DeviceTokenRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceTokenRepositoryImpl implements DeviceTokenRepository {

	private final DeviceTokenJpaRepository jpa;

	/** 상태 변경 중심 **/

	@Override
	public DeviceToken save(final DeviceToken token) {
		return jpa.save(token);
	}

	@Override
	public int deleteByMemberIdAndToken(final Long memberId, final String token) {
		return jpa.deleteByMemberIdAndToken(memberId, token);
	}

	@Override
	public void deactivateTokens(final List<String> tokens) {
		jpa.deactivateTokens(tokens);
	}

	@Override
	public Optional<DeviceToken> findByMemberId(final Long memberId) {
		return jpa.findByMemberId(memberId);
	}

	/** 조회(읽기) 중심 **/
	@Override
	public List<String> findActiveTokensByMemberIds(final List<Long> memberIds) {
		return jpa.findActiveTokensByMemberIds(memberIds);
	}

	@Override
	public boolean existsActiveByMemberId(Long memberId) {
		return jpa.existsActiveByMemberId(memberId);
	}

}
