# 🍎 Apple 로그인 아키텍처 문서

## 📋 목차
1. [개요](#개요)
2. [전체 플로우](#전체-플로우)
3. [컴포넌트 설명](#컴포넌트-설명)
4. [시퀀스 다이어그램](#시퀀스-다이어그램)
5. [보안 고려사항](#보안-고려사항)
6. [트러블슈팅](#트러블슈팅)

---

## 개요

### 기술 스택
- **OAuth 2.0**: Authorization Code Flow
- **JWT**: ES256 (client_secret), RS256 (id_token)
- **HTTP 클라이언트**: Spring HTTP Interface
- **캐싱**: Caffeine Cache (24시간 TTL)

### 주요 특징
- ✅ Kakao 로그인과 동일한 구조 (대칭적 설계)
- ✅ Client Secret을 JWT로 동적 생성 (Private Key 사용)
- ✅ id_token RSA-256 서명 검증
- ✅ Apple 공개 키 캐싱 (성능 최적화)
- ✅ Multi-provider OAuth 지원

---

## 전체 플로우

### 1. 사용자 인증 플로우

```
┌─────────┐         ┌──────────┐         ┌─────────┐         ┌──────────────┐
│  User   │         │ Frontend │         │ Backend │         │ Apple Server │
└────┬────┘         └────┬─────┘         └────┬────┘         └──────┬───────┘
     │                   │                     │                     │
     │ 1. 로그인 요청     │                     │                     │
     ├──────────────────>│                     │                     │
     │                   │                     │                     │
     │                   │ 2. Apple 인증 페이지로 리다이렉트            │
     │                   ├─────────────────────────────────────────>│
     │                   │                     │                     │
     │ 3. Apple 로그인    │                     │                     │
     ├───────────────────────────────────────────────────────────>│
     │                   │                     │                     │
     │                   │ 4. Authorization Code + Redirect          │
     │                   │<─────────────────────────────────────────┤
     │                   │                     │                     │
     │                   │ 5. GET /api/auth/login/oauth2/code/apple?code=xxx
     │                   ├────────────────────>│                     │
     │                   │                     │                     │
     │                   │                     │ 6. Token 요청        │
     │                   │                     │   (code + client_secret JWT)
     │                   │                     ├────────────────────>│
     │                   │                     │                     │
     │                   │                     │ 7. id_token 응답     │
     │                   │                     │<────────────────────┤
     │                   │                     │                     │
     │                   │                     │ 8. 공개 키 가져오기   │
     │                   │                     ├────────────────────>│
     │                   │                     │<────────────────────┤
     │                   │                     │                     │
     │                   │                     │ 9. id_token 검증     │
     │                   │                     │   (서명 + claims)    │
     │                   │                     │                     │
     │                   │ 10. 로그인 처리      │                     │
     │                   │    (신규/기존 사용자) │                     │
     │                   │<────────────────────┤                     │
     │                   │                     │                     │
```

### 2. 코드 레벨 플로우

```
AuthController.appleCallback(code)
  │
  └─> AuthFacade.handleOAuthLogin("apple", code)
       │
       └─> AuthService.fetchOAuthUserInfo("apple", code)
            │
            └─> AppleOAuthClient
                 │
                 ├─> 1. fetchAccessToken(code)
                 │    │
                 │    ├─> generateClientSecret()
                 │    │    └─> ES256 JWT 생성 (Private Key 사용)
                 │    │
                 │    └─> AppleApiHttpClient.getIdToken()
                 │         └─> POST https://appleid.apple.com/auth/token
                 │
                 └─> 2. fetchUserInfo(idToken)
                      │
                      ├─> extractKid(idToken)
                      │    └─> JWT 헤더에서 kid 추출
                      │
                      ├─> fetchPublicKeys() [@Cacheable]
                      │    └─> AppleApiHttpClient.getPublicKeys()
                      │         └─> GET https://appleid.apple.com/auth/keys
                      │
                      ├─> generatePublicKey(n, e)
                      │    └─> RSA PublicKey 생성
                      │
                      └─> validateAndExtractUserId(idToken, publicKey)
                           └─> RS256 서명 검증 + Claims 확인
```

---

## 컴포넌트 설명

### 1. AppleOAuthClient

**위치**: `com.runky.auth.infrastructure.erternal.AppleOAuthClient`

**역할**: Apple OAuth 2.0 플로우의 모든 로직을 처리

**주요 메서드**:

#### 1.1 fetchAccessToken(authorizationCode)
```java
@Override
public String fetchAccessToken(String authorizationCode)
```

**입력**: Authorization code (Apple에서 받은 임시 코드)
**출력**: id_token (JWT)

**동작**:
1. `generateClientSecret()` 호출하여 client_secret JWT 생성
2. Apple Token Endpoint에 POST 요청
   - client_id: `api.runky.store`
   - client_secret: ES256 JWT
   - code: authorization code
   - grant_type: `authorization_code`
   - redirect_uri: `https://api.runky.store/api/auth/login/oauth2/code/apple`
3. 응답에서 `id_token` 추출하여 반환

#### 1.2 fetchUserInfo(idToken)
```java
@Override
public OAuthUserInfo fetchUserInfo(String idToken)
```

**입력**: id_token (JWT)
**출력**: OAuthUserInfo (provider="apple", providerId="사용자ID")

**동작**:
1. `extractKid(idToken)`: JWT 헤더에서 kid 추출
2. `fetchPublicKeys()`: Apple 공개 키 목록 가져오기 (캐싱됨)
3. kid와 매칭되는 공개 키 찾기
4. `generatePublicKey()`: RSA 공개 키 생성
5. JWT 서명 검증 (RS256)
6. Claims 확인 (subject, issuer, audience, expiration)
7. Apple User ID 반환

#### 1.3 generateClientSecret() [private]
```java
private String generateClientSecret()
```

**역할**: ES256 JWT client_secret 생성

**JWT 구조**:
```json
{
  "header": {
    "kid": "P6XD5963AF",
    "alg": "ES256"
  },
  "payload": {
    "iss": "UZ3ZMHV48B",      // Team ID
    "iat": 1637123456,         // 현재 시간
    "exp": 1652675456,         // 만료 시간 (6개월 후)
    "aud": "https://appleid.apple.com",
    "sub": "api.runky.store"   // Service ID
  },
  "signature": "..." // EC Private Key로 서명
}
```

#### 1.4 fetchPublicKeys() [private, @Cacheable]
```java
@Cacheable(value = "applePublicKeys", unless = "#result == null")
private ApplePublicKeys fetchPublicKeys()
```

**역할**: Apple 공개 키 목록 가져오기

**캐싱**:
- 캐시 이름: `applePublicKeys`
- TTL: 24시간 (CacheConfig에서 설정)
- 이유: Apple 공개 키는 자주 변경되지 않음

**응답 예시**:
```json
{
  "keys": [
    {
      "kty": "RSA",
      "kid": "W6WcOKB",
      "use": "sig",
      "alg": "RS256",
      "n": "base64-encoded-modulus",
      "e": "AQAB"
    }
  ]
}
```

---

### 2. AppleApiHttpClient

**위치**: `com.runky.auth.infrastructure.erternal.AppleApiHttpClient`

**역할**: Apple API HTTP 통신 (Spring HTTP Interface)

**메서드**:

#### 2.1 getIdToken()
```java
@PostExchange(url = "https://appleid.apple.com/auth/token",
              contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
AppleTokenResponse getIdToken(@RequestBody MultiValueMap<String, String> body);
```

**요청**:
```
POST https://appleid.apple.com/auth/token
Content-Type: application/x-www-form-urlencoded

client_id=api.runky.store
&client_secret=eyJhbGc...
&code=c1234567890abcdef
&grant_type=authorization_code
&redirect_uri=https://api.runky.store/api/auth/login/oauth2/code/apple
```

**응답**:
```json
{
  "access_token": "a1234567890...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "r1234567890...",
  "id_token": "eyJhbGc..."
}
```

#### 2.2 getPublicKeys()
```java
@GetExchange("https://appleid.apple.com/auth/keys")
ApplePublicKeys getPublicKeys();
```

**요청**:
```
GET https://appleid.apple.com/auth/keys
```

**응답**: Apple 공개 키 목록 (JWK Set)

---

### 3. DTO 구조

#### 3.1 AppleTokenResponse
```java
public record AppleTokenResponse(
    String accessToken,   // OAuth 2.0 access token
    String tokenType,     // "Bearer"
    Long expiresIn,       // 3600 (1시간)
    String refreshToken,  // 갱신 토큰
    String idToken        // 사용자 정보 포함 JWT
)
```

#### 3.2 ApplePublicKeys
```java
public record ApplePublicKeys(
    List<ApplePublicKey> keys  // 공개 키 목록
)
```

#### 3.3 ApplePublicKey
```java
public record ApplePublicKey(
    String kty,  // "RSA"
    String kid,  // 키 ID (JWT 헤더의 kid와 매칭)
    String use,  // "sig" (서명용)
    String alg,  // "RS256"
    String n,    // RSA modulus (Base64 URL)
    String e     // RSA exponent (Base64 URL)
)
```

---

### 4. 설정 (AppleProperties)

**위치**: `com.runky.auth.config.props.AppleProperties`

```java
@ConfigurationProperties(prefix = "apple")
public record AppleProperties(
    String serviceId,    // Service ID (client_id)
    String teamId,       // Apple Developer Team ID
    String keyId,        // Private Key ID
    String privateKey,   // EC Private Key (PEM)
    String redirectUrl   // OAuth Callback URL
)
```

**application.yml**:
```yaml
apple:
  service-id: api.runky.store
  team-id: UZ3ZMHV48B
  key-id: P6XD5963AF
  private-key: |
    -----BEGIN PRIVATE KEY-----
    MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQg...
    -----END PRIVATE KEY-----
  redirect-url: https://api.runky.store/api/auth/login/oauth2/code/apple
```

---

### 5. 캐시 설정 (CacheConfig)

**위치**: `com.runky.auth.config.CacheConfig`

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("applePublicKeys");
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)  // 24시간 TTL
            .maximumSize(10));                      // 최대 10개
        return cacheManager;
    }
}
```

**캐싱 효과**:
- 첫 번째 요청: Apple 서버에서 공개 키 가져옴
- 이후 24시간: 캐시에서 공개 키 조회 (네트워크 요청 없음)
- 성능 향상 + Apple API 호출 감소

---

## 시퀀스 다이어그램

### Authorization Code → id_token 교환

```
┌──────────────────┐          ┌─────────────────┐          ┌──────────────┐
│ AppleOAuthClient │          │ AppleApiClient  │          │ Apple Server │
└────────┬─────────┘          └────────┬────────┘          └──────┬───────┘
         │                              │                          │
         │ 1. generateClientSecret()    │                          │
         ├──────────────┐               │                          │
         │              │               │                          │
         │<─────────────┘               │                          │
         │ ES256 JWT                    │                          │
         │                              │                          │
         │ 2. getIdToken(params)        │                          │
         ├─────────────────────────────>│                          │
         │                              │                          │
         │                              │ 3. POST /auth/token      │
         │                              ├─────────────────────────>│
         │                              │                          │
         │                              │ 4. id_token 응답          │
         │                              │<─────────────────────────┤
         │                              │                          │
         │ 5. AppleTokenResponse        │                          │
         │<─────────────────────────────┤                          │
         │                              │                          │
         │ 6. return id_token           │                          │
         │                              │                          │
```

### id_token 검증 플로우

```
┌──────────────────┐          ┌─────────────────┐          ┌──────────────┐
│ AppleOAuthClient │          │ Cache (Caffeine)│          │ Apple Server │
└────────┬─────────┘          └────────┬────────┘          └──────┬───────┘
         │                              │                          │
         │ 1. extractKid(idToken)       │                          │
         ├──────────────┐               │                          │
         │              │               │                          │
         │<─────────────┘               │                          │
         │ kid                          │                          │
         │                              │                          │
         │ 2. fetchPublicKeys()         │                          │
         ├─────────────────────────────>│                          │
         │                              │                          │
         │                              │ 3. Cache Hit?            │
         │                              ├────────┐                 │
         │                              │        │                 │
         │                              │<───────┘                 │
         │                              │ Yes: Return cached       │
         │ 4. ApplePublicKeys           │                          │
         │<─────────────────────────────┤                          │
         │                              │                          │
         │                              │ No: GET /auth/keys       │
         │                              ├─────────────────────────>│
         │                              │<─────────────────────────┤
         │                              │ Cache & Return           │
         │<─────────────────────────────┤                          │
         │                              │                          │
         │ 5. Find key by kid           │                          │
         ├──────────────┐               │                          │
         │              │               │                          │
         │<─────────────┘               │                          │
         │ ApplePublicKey               │                          │
         │                              │                          │
         │ 6. generatePublicKey(n, e)   │                          │
         ├──────────────┐               │                          │
         │              │               │                          │
         │<─────────────┘               │                          │
         │ RSA PublicKey                │                          │
         │                              │                          │
         │ 7. JWT verify (RS256)        │                          │
         ├──────────────┐               │                          │
         │              │               │                          │
         │<─────────────┘               │                          │
         │ Claims                       │                          │
         │                              │                          │
         │ 8. Extract User ID           │                          │
         │                              │                          │
```

---

## 보안 고려사항

### 1. Client Secret 보안

**문제**: Apple은 정적 client_secret을 제공하지 않음

**해결**: ES256 JWT를 동적 생성
- Private Key는 환경 변수로 관리
- JWT는 매 요청마다 새로 생성 (6개월 유효)
- Private Key 노출 방지 (application.yml에서 환경 변수 사용)

### 2. id_token 검증

**검증 항목**:
1. **서명 검증**: Apple 공개 키로 RS256 서명 확인
2. **Issuer 확인**: `iss == "https://appleid.apple.com"`
3. **Audience 확인**: `aud == "api.runky.store"`
4. **만료 시간**: `exp > 현재 시간`
5. **Kid 매칭**: JWT 헤더의 kid가 공개 키에 존재

### 3. 공개 키 캐싱

**보안 vs 성능 트레이드오프**:
- 캐싱 없음: 매 요청마다 Apple 서버 호출 (느림, 안전)
- 24시간 캐싱: 성능 향상, Apple 키 변경 시 최대 24시간 지연

**권장**: 24시간 캐싱 (Apple 키는 거의 변경되지 않음)

---

## 트러블슈팅

### 1. "Apple client secret 생성 실패"

**원인**:
- Private Key 형식 오류
- Key ID 불일치
- Team ID 불일치

**해결**:
```bash
# Private Key 확인
echo "$APPLE_PRIVATE_KEY" | openssl ec -text -noout

# Key ID 확인 (Apple Developer Console)
# Team ID 확인 (Apple Developer Console)
```

### 2. "Apple 공개 키를 찾을 수 없습니다"

**원인**:
- JWT kid와 Apple 공개 키의 kid가 매칭되지 않음
- id_token이 만료되었거나 손상됨

**해결**:
```bash
# id_token 디코딩
echo "eyJhbGc..." | base64 -d

# kid 확인
# Apple 공개 키 목록 확인
curl https://appleid.apple.com/auth/keys
```

### 3. "JWT 검증 실패"

**원인**:
- 서명 불일치
- 만료된 토큰
- 잘못된 공개 키

**해결**:
```java
// 로그 확인
log.error("Apple identity token 검증 실패", e);

// Claims 확인
Claims claims = ...;
log.info("iss: {}", claims.getIssuer());
log.info("aud: {}", claims.getAudience());
log.info("exp: {}", claims.getExpiration());
```

### 4. "Cache 미작동"

**원인**:
- `@EnableCaching` 누락
- CacheConfig 빈 등록 안됨
- Caffeine 의존성 없음

**해결**:
```java
// CacheConfig 확인
@Configuration
@EnableCaching  // 이 어노테이션 필수!
public class CacheConfig { ... }

// build.gradle 확인
implementation 'com.github.ben-manes.caffeine:caffeine'
```

---

## 참고 자료

- [Apple - Sign in with Apple REST API](https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_rest_api)
- [Apple - Generate and Validate Tokens](https://developer.apple.com/documentation/sign_in_with_apple/generate_and_validate_tokens)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [Caffeine Cache](https://github.com/ben-manes/caffeine)
- [Spring HTTP Interface](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface)
