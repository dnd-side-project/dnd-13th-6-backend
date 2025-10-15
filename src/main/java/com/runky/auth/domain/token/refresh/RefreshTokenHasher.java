package com.runky.auth.domain.token.refresh;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.runky.auth.config.props.RefreshTokenHashProperties;
import com.runky.auth.exception.infra.TokenBlankException;
import com.runky.auth.exception.infra.TokenHashAlgorithmUnsupportedException;
import com.runky.auth.exception.infra.TokenHashFailureException;
import com.runky.auth.exception.infra.TokenHashPepperBlankException;

import lombok.RequiredArgsConstructor;

/**
 * 리프레시 토큰 원문을 저장하지 않도록 해시를 생성합니다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenHasher {

	private static final String ALGORITHM = "HmacSHA256";

	private final RefreshTokenHashProperties props;

	public String hash(String token) {
		if (token == null || token.isBlank()) {
			throw new TokenBlankException();
		}
		String pepper = props.pepper();
		if (pepper == null || pepper.isBlank()) {
			throw new TokenHashPepperBlankException();
		}

		try {
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8), ALGORITHM));
			byte[] out = mac.doFinal(token.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(out);
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new TokenHashAlgorithmUnsupportedException();
		} catch (Exception e) {
			throw new TokenHashFailureException();
		}
	}
}
