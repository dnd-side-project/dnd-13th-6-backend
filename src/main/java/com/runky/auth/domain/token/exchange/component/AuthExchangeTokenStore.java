package com.runky.auth.domain.token.exchange.component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthExchangeTokenStore {

	private final Map<String, TokenData> store = new ConcurrentHashMap<>();

	/**
	 * 토큰 저장
	 * @param tokenId 토큰 ID
	 * @param memberId 회원 ID
	 * @param expiresAtMillis 만료시간 (milliseconds)
	 */
	public void save(String tokenId, Long memberId, long expiresAtMillis) {
		store.put(buildKey(tokenId), new TokenData(memberId, expiresAtMillis));
		log.debug("Auth exchange token saved: {}", tokenId);
	}

	public Optional<Long> retrieve(String tokenId) {
		String key = buildKey(tokenId);
		TokenData data = store.get(key);

		if (data == null) {
			return Optional.empty();
		}

		if (data.isExpired()) {
			store.remove(key);
			log.debug("Auth exchange token expired: {}", tokenId);
			return Optional.empty();
		}

		return Optional.of(data.memberId());
	}

	public boolean exists(String tokenId) {
		String key = buildKey(tokenId);
		TokenData data = store.get(key);

		if (data == null) {
			return false;
		}

		if (data.isExpired()) {
			store.remove(key);
			return false;
		}

		return true;
	}

	public void delete(String tokenId) {
		store.remove(buildKey(tokenId));
		log.debug("Auth exchange token deleted: {}", tokenId);
	}

	private String buildKey(String tokenId) {
		return "auth:exchange:" + tokenId;
	}

	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	public void cleanupExpired() {
		long now = System.currentTimeMillis();
		int removedCount = 0;

		for (Map.Entry<String, TokenData> entry : store.entrySet()) {
			if (entry.getValue().expiresAt() < now) {
				store.remove(entry.getKey());
				removedCount++;
			}
		}

		if (removedCount > 0) {
			log.info("Cleaned up {} expired auth exchange tokens", removedCount);
		}
	}

	/**
	 * 토큰 데이터 (memberId + 만료시간)
	 */
	private record TokenData(Long memberId, long expiresAt) {
		boolean isExpired() {
			return System.currentTimeMillis() > expiresAt;
		}
	}
}
