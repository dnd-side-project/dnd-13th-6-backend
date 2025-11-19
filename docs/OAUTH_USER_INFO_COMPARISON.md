# OAuth 사용자 정보 획득 방식 비교: 카카오 vs 애플

## 핵심 차이점

카카오와 애플은 **사용자 정보를 가져오는 방식**이 근본적으로 다릅니다.

| 구분 | 카카오 | 애플 |
|------|--------|------|
| 토큰 교환 결과 | Access Token | id_token (JWT) |
| 사용자 정보 획득 | 별도 API 호출 필요 | JWT 파싱으로 획득 |
| HTTP 요청 횟수 | 2번 (토큰 교환 + 사용자 정보) | 1번 (토큰 교환만) |
| 검증 방식 | 카카오 서버에 요청 | 로컬에서 JWT 서명 검증 |

---

## 카카오 로그인: Access Token + 사용자 정보 API

### 1단계: Authorization Code → Access Token

```
POST https://kauth.kakao.com/oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&client_id={CLIENT_ID}
&redirect_uri={REDIRECT_URI}
&code={AUTHORIZATION_CODE}

Response:
{
  "access_token": "xxxxxxxxxxxxxx",
  "token_type": "bearer",
  "expires_in": 21599
}
```

### 2단계: Access Token으로 사용자 정보 조회

```
GET https://kapi.kakao.com/v2/user/me
Authorization: Bearer {ACCESS_TOKEN}

Response:
{
  "id": 1234567890,
  "connected_at": "2024-01-01T00:00:00Z",
  "kakao_account": {
    "email": "user@example.com"
  }
}
```

### 코드 구현

```java
@Component
public class KakaoOAuthClient implements OAuthClient {

    private final KakaoApiHttpClient kakaoApiHttpClient;

    @Override
    public String fetchAccessToken(String authorizationCode) {
        // 1단계: Authorization Code → Access Token 교환
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", props.clientId());
        body.add("redirect_uri", props.redirectUrl());
        body.add("code", authorizationCode);

        KakaoTokenResponse tokenResponse = kakaoApiHttpClient.getAccessToken(body);

        return tokenResponse.accessToken();  // Access Token 반환
    }

    @Override
    public OAuthUserInfo fetchUserInfo(String accessToken) {
        // 2단계: Access Token으로 별도 API 호출
        String authorizationHeader = "Bearer " + accessToken;

        KakaoUserInfoResponse userInfoResponse = kakaoApiHttpClient.getUserInfo(authorizationHeader);

        return new OAuthUserInfo(
            "kakao",
            String.valueOf(userInfoResponse.id())
        );
    }
}
```

### KakaoApiHttpClient

```java
public interface KakaoApiHttpClient {

    @PostExchange(url = "https://kauth.kakao.com/oauth/token")
    KakaoTokenResponse getAccessToken(@RequestBody MultiValueMap<String, String> body);

    @GetExchange(url = "https://kapi.kakao.com/v2/user/me")
    KakaoUserInfoResponse getUserInfo(@RequestHeader("Authorization") String authorizationHeader);
}
```

**특징**:
- Access Token은 **불투명한 문자열** (Opaque Token)
- 사용자 정보를 얻으려면 **반드시 카카오 서버에 추가 HTTP 요청** 필요
- 카카오 서버가 Access Token을 검증하고 사용자 정보 반환

---

## 애플 로그인: id_token (JWT) 직접 파싱

### 1단계: Authorization Code → id_token

```
POST https://appleid.apple.com/auth/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&client_id={SERVICE_ID}
&client_secret={GENERATED_JWT}
&code={AUTHORIZATION_CODE}
&redirect_uri={REDIRECT_URI}

Response:
{
  "access_token": "xxxxxx",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "xxxxxx",
  "id_token": "eyJraWQiOiJXNldjT0tCIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoiYXBpLnJ1bmt5LnN0b3JlIiwiZXhwIjoxNjQwOTk1MjAwLCJpYXQiOjE2NDA5MDg4MDAsInN1YiI6IjAwMTIzNC5hYmNkZWZnaGlqayIsImF0X2hhc2giOiJ4eHh4In0.signature"
}
```

### 2단계: id_token 자체에서 사용자 정보 추출 (별도 API 호출 없음)

id_token은 JWT 형식으로, 3부분으로 구성됩니다:

```
eyJraWQiOiJXNldjT0tCIiwiYWxnIjoiUlMyNTYifQ  // Header (Base64)
.
eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoiYXBpLnJ1bmt5LnN0b3JlIiwiZXhwIjoxNjQwOTk1MjAwLCJpYXQiOjE2NDA5MDg4MDAsInN1YiI6IjAwMTIzNC5hYmNkZWZnaGlqayIsImF0X2hhc2giOiJ4eHh4In0  // Payload (Base64)
.
signature  // Signature
```

#### Header (디코딩 후):
```json
{
  "kid": "W6WcOKB",
  "alg": "RS256"
}
```

#### Payload (디코딩 후):
```json
{
  "iss": "https://appleid.apple.com",
  "aud": "api.runky.store",
  "exp": 1640995200,
  "iat": 1640908800,
  "sub": "001234.abcdefghijk",  // ← Apple User ID
  "at_hash": "xxxx"
}
```

**`sub` 필드가 바로 Apple User ID입니다!**

### 3단계: JWT 검증 (Apple 공개 키 사용)

```
GET https://appleid.apple.com/auth/keys

Response:
{
  "keys": [
    {
      "kty": "RSA",
      "kid": "W6WcOKB",
      "use": "sig",
      "alg": "RS256",
      "n": "modulus_base64",
      "e": "exponent_base64"
    }
  ]
}
```

### 코드 구현

```java
@Component("appleOAuthClient")
public class AppleOAuthClient implements OAuthClient {

    private final AppleApiHttpClient appleApiHttpClient;

    @Override
    public String fetchAccessToken(String authorizationCode) {
        // 1단계: Authorization Code → id_token 교환
        String clientSecret = generateClientSecret();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", props.serviceId());
        body.add("client_secret", clientSecret);
        body.add("code", authorizationCode);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", props.redirectUrl());

        AppleTokenResponse tokenResponse = appleApiHttpClient.getAccessToken(body);

        return tokenResponse.idToken();  // id_token 반환 (JWT 형식)
    }

    @Override
    public OAuthUserInfo fetchUserInfo(String idToken) {
        // 2단계: 별도 API 호출 없이 JWT 검증 및 파싱
        String appleUserId = validateAndExtractUserId(idToken);

        return new OAuthUserInfo("apple", appleUserId);
    }

    private String validateAndExtractUserId(String idToken) {
        // 1. JWT Header에서 kid 추출
        String kid = extractKid(idToken);

        // 2. Apple 공개 키 목록 가져오기 (캐시됨)
        ApplePublicKeys publicKeys = fetchPublicKeys();

        // 3. kid와 일치하는 공개 키 찾기
        ApplePublicKey matchingKey = publicKeys.keys().stream()
            .filter(key -> key.kid().equals(kid))
            .findFirst()
            .orElseThrow(() -> new AuthenticationException("Apple 공개 키를 찾을 수 없습니다."));

        // 4. RSA 공개 키 생성
        PublicKey publicKey = generatePublicKey(matchingKey);

        // 5. JWT 서명 검증 및 Payload 추출
        Claims claims = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(idToken)
            .getPayload();

        // 6. subject에서 Apple User ID 추출
        String appleUserId = claims.getSubject();
        if (appleUserId == null || appleUserId.isBlank()) {
            throw new AuthenticationException("Apple User ID가 없습니다.");
        }

        return appleUserId;
    }
}
```

### AppleApiHttpClient

```java
public interface AppleApiHttpClient {

    @PostExchange(url = "https://appleid.apple.com/auth/token")
    AppleTokenResponse getAccessToken(@RequestBody MultiValueMap<String, String> body);

    @GetExchange("https://appleid.apple.com/auth/keys")
    ApplePublicKeys getPublicKeys();
}
```

**특징**:
- id_token은 **JWT (JSON Web Token)** 형식
- 사용자 정보(subject)가 **JWT Payload에 이미 포함**되어 있음
- **별도 API 호출 없이** JWT를 로컬에서 검증하고 파싱하면 사용자 정보 획득 가능
- Apple 공개 키는 **JWT 서명 검증용**이며, 24시간 캐시됨

---

## 애플이 별도 사용자 정보 API를 제공하지 않는 이유

### 1. OpenID Connect (OIDC) 표준 준수

애플은 **OpenID Connect** 프로토콜을 따릅니다. OIDC에서는:
- `access_token`: 리소스 서버 접근용 (선택적)
- `id_token`: 사용자 인증 정보 전달용 (필수)

id_token에 사용자 정보가 포함되어 있으므로, 별도 UserInfo 엔드포인트가 필수가 아닙니다.

### 2. 프라이버시 중심 설계

애플은 사용자 프라이버시를 강조합니다:
- 최소한의 정보만 제공 (User ID만 포함)
- 서버 간 통신 최소화
- 클라이언트가 JWT를 로컬에서 검증 가능

### 3. 서버 부하 감소

- 매번 사용자 정보 API를 호출할 필요가 없음
- JWT 검증은 공개 키만 있으면 로컬에서 가능
- Apple 서버 부하 감소

### 4. 보안

- JWT는 서명되어 있어 변조 불가능
- 공개 키 암호화 방식으로 신뢰성 보장
- 토큰 유효기간(exp) 자체에 포함

---

## 전체 플로우 비교

### 카카오 로그인

```
[Client] → Authorization Code
    ↓
[Server] → POST /oauth/token → [Kakao]
    ↓
[Server] ← Access Token
    ↓
[Server] → GET /v2/user/me (with Bearer Token) → [Kakao]
    ↓
[Server] ← User Info (id, email, etc.)
    ↓
[Server] → 회원가입/로그인 처리
```

**총 HTTP 요청**: 2번
- 토큰 교환: 1번
- 사용자 정보 조회: 1번

### 애플 로그인

```
[Client] → Authorization Code
    ↓
[Server] → POST /auth/token → [Apple]
    ↓
[Server] ← id_token (JWT)
    ↓
[Server] → GET /auth/keys → [Apple] (최초 1회, 이후 캐시)
    ↓
[Server] ← Public Keys
    ↓
[Server] → JWT 검증 및 파싱 (로컬)
    ↓
[Server] → subject에서 User ID 추출 (로컬)
    ↓
[Server] → 회원가입/로그인 처리
```

**총 HTTP 요청**: 1번 (공개 키는 캐시됨)
- 토큰 교환: 1번
- 사용자 정보 추출: 0번 (JWT 파싱)

---

## JWT 검증 과정 상세

### 1. Header에서 kid 추출

```java
private String extractKid(String idToken) {
    String[] parts = idToken.split("\\.");  // Header.Payload.Signature
    String header = new String(Base64.getUrlDecoder().decode(parts[0]));

    Map<String, Object> headerMap = objectMapper.readValue(header, Map.class);
    String kid = (String) headerMap.get("kid");  // "W6WcOKB"

    return kid;
}
```

### 2. kid로 Apple 공개 키 찾기

```java
@Cacheable(value = "applePublicKeys", unless = "#result == null")
private ApplePublicKeys fetchPublicKeys() {
    // 24시간 캐시됨 - 매번 호출하지 않음
    return appleApiHttpClient.getPublicKeys();
}

ApplePublicKey matchingKey = publicKeys.keys().stream()
    .filter(key -> key.kid().equals(kid))
    .findFirst()
    .orElseThrow();
```

### 3. RSA 공개 키 생성

```java
private PublicKey generatePublicKey(ApplePublicKey publicKey) {
    byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.n());
    byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.e());

    BigInteger n = new BigInteger(1, nBytes);  // modulus
    BigInteger e = new BigInteger(1, eBytes);  // exponent

    RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");

    return keyFactory.generatePublic(publicKeySpec);
}
```

### 4. JWT 서명 검증 및 Claims 추출

```java
Claims claims = Jwts.parser()
    .verifyWith(publicKey)  // RSA-256 서명 검증
    .build()
    .parseSignedClaims(idToken)
    .getPayload();

String appleUserId = claims.getSubject();  // "001234.abcdefghijk"
```

검증 내용:
- 서명(Signature)이 유효한지 확인
- 발급자(iss)가 "https://appleid.apple.com"인지 확인
- 대상(aud)이 우리 service-id인지 확인
- 만료 시간(exp)이 지나지 않았는지 확인

---

## 결론

| 항목 | 카카오 | 애플 |
|------|--------|------|
| 토큰 형식 | Opaque Token (불투명) | JWT (자체 포함) |
| 사용자 정보 위치 | 카카오 서버 | JWT Payload |
| 정보 획득 방식 | API 호출 필요 | JWT 파싱 |
| HTTP 요청 | 2번 | 1번 |
| 검증 주체 | 카카오 서버 | 로컬 (공개 키 사용) |
| 표준 | OAuth 2.0 | OpenID Connect |

**애플은 사용자 정보 조회 API 없이 id_token(JWT)만으로 모든 것을 해결합니다.**
