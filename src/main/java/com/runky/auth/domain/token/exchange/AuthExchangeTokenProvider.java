package com.runky.auth.domain.token.exchange;

import org.springframework.stereotype.Component;

import com.runky.auth.domain.token.exchange.component.AuthExchangeTokenFactory;
import com.runky.auth.domain.token.exchange.component.AuthExchangeTokenParser;
import com.runky.auth.domain.token.exchange.component.AuthExchangeTokenStore;
import com.runky.auth.domain.token.exchange.vo.AuthExchangeToken;
import com.runky.auth.exception.domain.InvalidTokenException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthExchangeTokenProvider {

	private final AuthExchangeTokenFactory factory;
	private final AuthExchangeTokenParser tokenParser;
	private final AuthExchangeTokenStore tokenStore;

	// ===== 생성 =====
	public String createExchangeToken(Long memberId) {
		// 1. Factory로 토큰 생성 (ID + 만료시간)
		AuthExchangeToken token = factory.create();

		// 2. Store에 저장 (memberId + 만료시간)
		tokenStore.save(
			token.idValue(),
			memberId,
			token.getExpiresAt().toEpochMilli()
		);

		log.info("Auth exchange token created for memberId: {}", memberId);

		return token.idValue();
	}

	// ===== 검증 =====
	public boolean validate(String token) {
		return tokenParser.validate(token);
	}

	// ===== 파싱 & 소비 (일회용) =====
	public Long consumeToken(String token) {
		if (!validate(token)) {
			throw new InvalidTokenException();
		}

		Long memberId = tokenParser.parse(token);

		// 일회용: 즉시 삭제 (Replay Attack 방지)
		tokenStore.delete(token);

		log.info("Auth exchange token consumed for memberId: {}", memberId);

		return memberId;
	}
}
