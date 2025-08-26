package com.runky.auth.domain.port;

import com.runky.auth.domain.vo.AccessTokenClaims;
import com.runky.auth.domain.vo.RefreshTokenClaims;

public interface TokenDecoder {
	AccessTokenClaims decodeAccess(String accessToken);

	RefreshTokenClaims decodeRefresh(String refreshToken);
}
