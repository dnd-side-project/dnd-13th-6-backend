package com.runky.notification.domain.push;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository {
	/** 상태 변경 중심 **/
	DeviceToken save(DeviceToken token);

	int deleteByMemberIdAndToken(Long memberId, String token);

	void deactivateTokens(List<String> tokens);

	Optional<DeviceToken> findByMemberIdAndToken(Long memberId, String token); // 운영/정합 목적

	/** 조회(읽기) 중심 **/
	List<String> findActiveTokensByMemberIds(List<Long> memberIds);

	boolean existsActiveByMemberIdAndToken(Long memberId, String token);

}
