package com.runky.auth.domain.token.signup.component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.runky.auth.domain.OAuthUserInfo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SignupTokenStore {

	private final Map<String, TokenData> store = new ConcurrentHashMap<>();

	/**
	 * 토큰 저장
	 * @param tokenId 토큰 ID
	 * @param oauthUserInfo OAuth 사용자 정보
	 * @param expiresAtMillis 만료시간 (milliseconds)
	 */
	public void save(String tokenId, OAuthUserInfo oauthUserInfo, long expiresAtMillis) {
		store.put(buildKey(tokenId), new TokenData(oauthUserInfo, expiresAtMillis));
		log.debug("Signup token saved: {}", tokenId);
	}

	public Optional<OAuthUserInfo> retrieve(String tokenId) {
		String key = buildKey(tokenId);
		TokenData data = store.get(key);

		if (data == null) {
			return Optional.empty();
		}

		// 만료 체크
		if (data.isExpired()) {
			store.remove(key);
			log.debug("Signup token expired: {}", tokenId);
			return Optional.empty();
		}

		return Optional.of(data.oauthUserInfo());
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
		log.debug("Signup token deleted: {}", tokenId);
	}

	private String buildKey(String tokenId) {
		return "signup:token:" + tokenId;
	}

	// 매일 자정 만료된 토큰 정리 (Asia/Seoul 기준)
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
			log.info("Cleaned up {} expired signup tokens", removedCount);
		}
	}

	/**
	 * 토큰 데이터 (OAuthUserInfo + 만료시간)
	 */
	private record TokenData(OAuthUserInfo oauthUserInfo, long expiresAt) {
		boolean isExpired() {
			return System.currentTimeMillis() > expiresAt;
		}
	}
}
