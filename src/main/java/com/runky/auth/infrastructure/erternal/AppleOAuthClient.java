package com.runky.auth.infrastructure.erternal;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.runky.auth.config.props.AppleProperties;
import com.runky.auth.domain.OAuthClient;
import com.runky.auth.domain.OAuthUserInfo;
import com.runky.auth.exception.AuthenticationException;
import com.runky.auth.infrastructure.erternal.dto.ApplePublicKey;
import com.runky.auth.infrastructure.erternal.dto.ApplePublicKeys;
import com.runky.auth.infrastructure.erternal.dto.AppleTokenResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("appleOAuthClient")
@RequiredArgsConstructor
public class AppleOAuthClient implements OAuthClient {

	private final AppleApiHttpClient appleApiHttpClient;
	private final AppleProperties props;

	@Override
	public String fetchAccessToken(String authorizationCode) {
		String clientSecret = generateClientSecret();

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("client_id", props.serviceId());
		body.add("client_secret", clientSecret);
		body.add("code", authorizationCode);
		body.add("grant_type", "authorization_code");
		body.add("redirect_uri", props.redirectUrl());

		AppleTokenResponse tokenResponse = appleApiHttpClient.getAccessToken(body);

		return tokenResponse.idToken();
	}

	@Override
	public String devFetchAccessToken(String authorizationCode) {
		String clientSecret = generateClientSecret();

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("client_id", props.serviceId());
		body.add("client_secret", clientSecret);
		body.add("code", authorizationCode);
		body.add("grant_type", "authorization_code");
		body.add("redirect_uri", "https://api.runky.store/api/auth/dev/login/oauth2/code/apple");

		AppleTokenResponse tokenResponse = appleApiHttpClient.getAccessToken(body);

		return tokenResponse.idToken();
	}

	@Override
	public String fetchAccessTokenForBranch(String authorizationCode, String branch) {
		return fetchAccessToken(authorizationCode);
	}

	@Override
	public OAuthUserInfo fetchUserInfo(String idToken) {
		String appleUserId = validateAndExtractUserId(idToken);

		return new OAuthUserInfo("apple", appleUserId);
	}

	private String generateClientSecret() {
		try {
			Date now = new Date();
			Date expirationDate = new Date(now.getTime() + 15777000000L);

			PrivateKey privateKey = getPrivateKey();

			return Jwts.builder()
				.header()
				.keyId(props.keyId())
				.and()
				.issuer(props.teamId())
				.issuedAt(now)
				.expiration(expirationDate)
				.audience().add("https://appleid.apple.com")
				.and()
				.subject(props.serviceId())
				.signWith(privateKey, Jwts.SIG.ES256)
				.compact();

		} catch (Exception e) {
			log.error("Apple client secret 생성 실패", e);
			throw new AuthenticationException("Apple client secret 생성에 실패했습니다: " + e.getMessage());
		}
	}

	private PrivateKey getPrivateKey() {
		try {
			String privateKeyContent = props.privateKey()
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s", "");

			byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("EC");

			return keyFactory.generatePrivate(keySpec);

		} catch (Exception e) {
			log.error("Private key 파싱 실패", e);
			throw new AuthenticationException("Private key 파싱에 실패했습니다: " + e.getMessage());
		}
	}

	private String validateAndExtractUserId(String idToken) {
		try {
			String kid = extractKid(idToken);
			ApplePublicKeys publicKeys = fetchPublicKeys();
			ApplePublicKey matchingKey = publicKeys.keys().stream()
				.filter(key -> key.kid().equals(kid))
				.findFirst()
				.orElseThrow(() -> new AuthenticationException("Apple 공개 키를 찾을 수 없습니다."));

			PublicKey publicKey = generatePublicKey(matchingKey);
			Claims claims = Jwts.parser()
				.verifyWith(publicKey)
				.build()
				.parseSignedClaims(idToken)
				.getPayload();

			String appleUserId = claims.getSubject();
			if (appleUserId == null || appleUserId.isBlank()) {
				throw new AuthenticationException("Apple User ID가 없습니다.");
			}

			log.info("Apple 로그인 검증 성공: userId={}", appleUserId);
			return appleUserId;

		} catch (Exception e) {
			log.error("Apple identity token 검증 실패", e);
			throw new AuthenticationException("Apple 로그인 검증에 실패했습니다: " + e.getMessage());
		}
	}

	private String extractKid(String idToken) {
		try {
			String[] parts = idToken.split("\\.");
			if (parts.length < 2) {
				throw new AuthenticationException("유효하지 않은 JWT 형식입니다.");
			}

			String header = new String(Base64.getUrlDecoder().decode(parts[0]));
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			@SuppressWarnings("unchecked")
			java.util.Map<String, Object> headerMap = mapper.readValue(header, java.util.Map.class);

			String kid = (String)headerMap.get("kid");
			if (kid == null) {
				throw new AuthenticationException("JWT 헤더에 kid가 없습니다.");
			}

			return kid;
		} catch (Exception e) {
			throw new AuthenticationException("JWT kid 추출 실패: " + e.getMessage());
		}
	}

	@Cacheable(value = "applePublicKeys", unless = "#result == null")
	private ApplePublicKeys fetchPublicKeys() {
		try {
			log.info("Apple 공개 키 fetching");
			ApplePublicKeys keys = appleApiHttpClient.getPublicKeys();

			if (keys == null || keys.keys() == null || keys.keys().isEmpty()) {
				throw new AuthenticationException("Apple 공개 키를 가져올 수 없습니다.");
			}

			log.info("Apple 공개 키 fetching 성공: {} keys", keys.keys().size());
			return keys;

		} catch (Exception e) {
			log.error("Apple 공개 키 fetching 실패", e);
			throw new AuthenticationException("Apple 공개 키를 가져오는데 실패했습니다: " + e.getMessage());
		}
	}

	private PublicKey generatePublicKey(ApplePublicKey publicKey) {
		try {
			byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.n());
			byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.e());

			BigInteger n = new BigInteger(1, nBytes);
			BigInteger e = new BigInteger(1, eBytes);

			RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			return keyFactory.generatePublic(publicKeySpec);
		} catch (Exception e) {
			throw new AuthenticationException("공개 키 생성 실패: " + e.getMessage());
		}
	}
}
