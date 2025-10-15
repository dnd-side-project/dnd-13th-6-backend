package com.runky.auth.domain.token.signup;

import org.springframework.stereotype.Component;

import com.runky.auth.domain.OAuthUserInfo;
import com.runky.auth.domain.token.signup.component.SignupTokenFactory;
import com.runky.auth.domain.token.signup.component.SignupTokenParser;
import com.runky.auth.domain.token.signup.component.SignupTokenStore;
import com.runky.auth.domain.token.signup.vo.SignupToken;
import com.runky.auth.exception.domain.InvalidTokenException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SignupTokenProvider {

	private final SignupTokenFactory factory;
	private final SignupTokenParser tokenParser;
	private final SignupTokenStore tokenStore;

	// ===== 생성 =====
	public String createSignupToken(OAuthUserInfo oauthUserInfo) {
		// 1. Factory로 토큰 생성 (ID + 만료시간)
		SignupToken token = factory.create();

		// 2. Store에 저장 (OAuthUserInfo + 만료시간)
		tokenStore.save(
			token.idValue(),
			oauthUserInfo,
			token.getExpiresAt().toEpochMilli()
		);

		log.info("Signup token created for provider: {}, providerId: {}",
			oauthUserInfo.provider(), oauthUserInfo.providerId());

		return token.idValue();
	}

	// ===== 검증 =====
	public boolean validate(String token) {
		return tokenParser.validate(token);
	}

	// ===== 파싱 & 소비 (일회용) =====
	public OAuthUserInfo consumeToken(String token) {
		if (!validate(token)) {
			throw new InvalidTokenException();
		}

		OAuthUserInfo oauthUserInfo = tokenParser.parse(token);

		// 일회용: 토큰 소비 후 삭제
		tokenStore.delete(token);

		log.info("Signup token consumed for provider: {}, providerId: {}",
			oauthUserInfo.provider(), oauthUserInfo.providerId());

		return oauthUserInfo;
	}
}
