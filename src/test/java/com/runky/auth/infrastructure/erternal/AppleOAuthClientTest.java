package com.runky.auth.infrastructure.erternal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;

import com.runky.auth.config.props.AppleProperties;
import com.runky.auth.domain.OAuthUserInfo;
import com.runky.auth.exception.AuthenticationException;
import com.runky.auth.infrastructure.erternal.dto.ApplePublicKey;
import com.runky.auth.infrastructure.erternal.dto.ApplePublicKeys;
import com.runky.auth.infrastructure.erternal.dto.AppleTokenResponse;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppleOAuthClient 테스트")
class AppleOAuthClientTest {

    @Mock
    private AppleApiHttpClient appleApiHttpClient;

    private AppleOAuthClient appleOAuthClient;

    private KeyPair rsaKeyPair;
    private String ecPrivateKeyPem;

    @BeforeEach
    void setUp() throws Exception {
        // RSA 키 쌍 생성 (id_token 서명용)
        KeyPairGenerator rsaGenerator = KeyPairGenerator.getInstance("RSA");
        rsaGenerator.initialize(2048);
        rsaKeyPair = rsaGenerator.generateKeyPair();

        // EC 키 쌍 생성 (client_secret 서명용)
        KeyPairGenerator ecGenerator = KeyPairGenerator.getInstance("EC");
        ecGenerator.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair ecKeyPair = ecGenerator.generateKeyPair();
        ecPrivateKeyPem = convertPrivateKeyToPem(ecKeyPair.getPrivate());

        // AppleProperties 생성
        AppleProperties appleProperties = new AppleProperties(
                "api.runky.store",
                "UZ3ZMHV48B",
                "P6XD5963AF",
                ecPrivateKeyPem,
                "https://api.runky.store/api/auth/login/oauth2/code/apple"
        );

        appleOAuthClient = new AppleOAuthClient(appleApiHttpClient, appleProperties);
    }

    @Test
    @DisplayName("Authorization code로 id_token을 성공적으로 가져온다")
    void fetchAccessToken_Success() {
        // given
        String authorizationCode = "test-auth-code";
        String expectedIdToken = "test-id-token";

        AppleTokenResponse tokenResponse = new AppleTokenResponse(
                "access-token",
                "Bearer",
                3600L,
                "refresh-token",
                expectedIdToken
        );

        given(appleApiHttpClient.getAccessToken(any(MultiValueMap.class))).willReturn(tokenResponse);

        // when
        String actualIdToken = appleOAuthClient.fetchAccessToken(authorizationCode);

        // then
        assertThat(actualIdToken).isEqualTo(expectedIdToken);
        verify(appleApiHttpClient, times(1)).getAccessToken(any(MultiValueMap.class));
    }

    @Test
    @DisplayName("id_token을 검증하고 사용자 정보를 반환한다")
    void fetchUserInfo_Success() throws Exception {
        // given
        String appleUserId = "001234.abcdefghijk";
        String kid = "test-kid";

        // RSA 공개 키를 ApplePublicKey DTO로 변환
        RSAPublicKey rsaPublicKey = (RSAPublicKey) rsaKeyPair.getPublic();
        String n = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(rsaPublicKey.getModulus().toByteArray());
        String e = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(rsaPublicKey.getPublicExponent().toByteArray());

        ApplePublicKey publicKey = new ApplePublicKey(
                "RSA",
                kid,
                "sig",
                "RS256",
                n,
                e
        );

        ApplePublicKeys publicKeys = new ApplePublicKeys(List.of(publicKey));

        // id_token 생성 (RSA 개인 키로 서명)
        String idToken = Jwts.builder()
                .header()
                .keyId(kid)
                .and()
                .subject(appleUserId)
                .issuer("https://appleid.apple.com")
                .audience().add("api.runky.store").and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(rsaKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        given(appleApiHttpClient.getPublicKeys()).willReturn(publicKeys);

        // when
        OAuthUserInfo userInfo = appleOAuthClient.fetchUserInfo(idToken);

        // then
        assertThat(userInfo.provider()).isEqualTo("apple");
        assertThat(userInfo.providerId()).isEqualTo(appleUserId);
    }

    @Test
    @DisplayName("잘못된 형식의 id_token은 검증 실패한다")
    void fetchUserInfo_InvalidJwtFormat_ThrowsException() {
        // given
        String invalidIdToken = "invalid.jwt.token";

        // when & then
        assertThatThrownBy(() -> appleOAuthClient.fetchUserInfo(invalidIdToken))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Apple 로그인 검증에 실패했습니다");
    }

    @Test
    @DisplayName("kid가 없는 id_token은 검증 실패한다")
    void fetchUserInfo_NoKid_ThrowsException() {
        // given
        String idTokenWithoutKid = Jwts.builder()
                .subject("test-user")
                .signWith(rsaKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        // when & then
        assertThatThrownBy(() -> appleOAuthClient.fetchUserInfo(idTokenWithoutKid))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("kid");
    }

    @Test
    @DisplayName("Dev 엔드포인트는 다른 redirect_uri를 사용한다")
    void devFetchAccessToken_UsesDifferentRedirectUri() {
        // given
        String authorizationCode = "test-auth-code";
        String expectedIdToken = "test-id-token";

        AppleTokenResponse tokenResponse = new AppleTokenResponse(
                "access-token",
                "Bearer",
                3600L,
                "refresh-token",
                expectedIdToken
        );

        given(appleApiHttpClient.getAccessToken(any(MultiValueMap.class))).willReturn(tokenResponse);

        // when
        String actualIdToken = appleOAuthClient.devFetchAccessToken(authorizationCode);

        // then
        assertThat(actualIdToken).isEqualTo(expectedIdToken);

        // redirect_uri 검증
        verify(appleApiHttpClient).getAccessToken(argThat(body -> {
            String redirectUri = body.getFirst("redirect_uri");
            return "https://api.runky.store/api/auth/dev/login/oauth2/code/apple".equals(redirectUri);
        }));
    }

    private String convertPrivateKeyToPem(java.security.PrivateKey privateKey) {
        String base64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" + base64 + "\n-----END PRIVATE KEY-----";
    }
}
