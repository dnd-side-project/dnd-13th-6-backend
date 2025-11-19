# Spring Map 자동 주입 설명

## Map<String, OAuthClient> 초기화

### 코드 위치
```java
// AuthService.java
private final Map<String, OAuthClient> oAuthClients;
```

### 초기화 시점

**Spring이 자동으로 초기화합니다!** 별도의 코드 작성이 필요 없습니다.

### 동작 원리

#### 1. Spring 컨테이너가 빈 스캔

```java
@Component("appleOAuthClient")  // 빈 이름: "appleOAuthClient"
public class AppleOAuthClient implements OAuthClient { ... }

@Component  // 빈 이름: "kakaoOAuthClient" (자동 생성)
public class KakaoOAuthClient implements OAuthClient { ... }
```

#### 2. 같은 타입의 빈들을 Map에 수집

Spring이 `OAuthClient` 인터페이스를 구현한 모든 빈을 찾아서 Map에 넣습니다:

```java
Map<String, OAuthClient> oAuthClients = {
    "appleOAuthClient" -> AppleOAuthClient 인스턴스,
    "kakaoOAuthClient" -> KakaoOAuthClient 인스턴스
}
```

#### 3. 생성자 주입

```java
@Service
@RequiredArgsConstructor  // Lombok이 생성자 자동 생성
public class AuthService {
    private final Map<String, OAuthClient> oAuthClients;  // Spring이 주입
}
```

### 빈 이름 규칙

| 선언 방식 | 빈 이름 | 예시 |
|----------|--------|------|
| `@Component` (명시 안함) | 클래스명의 첫 글자를 소문자로 | `KakaoOAuthClient` → `kakaoOAuthClient` |
| `@Component("이름")` | 명시한 이름 | `@Component("appleOAuthClient")` → `appleOAuthClient` |

### 사용 예시

```java
// AuthService.java
private OAuthClient getOAuthClient(String provider) {
    // provider가 "kakao"면 "kakaoOAuthClient" 조회
    // provider가 "apple"이면 "appleOAuthClient" 조회
    OAuthClient client = oAuthClients.get(provider + "OAuthClient");

    if (client == null) {
        throw new GlobalException(AuthErrorCode.UN_SUPPORT_AUTHENTICATION);
    }

    return client;
}
```

### 디버깅 방법

빈 이름 확인:
```java
@PostConstruct
public void init() {
    log.info("Registered OAuthClients: {}", oAuthClients.keySet());
    // 출력: Registered OAuthClients: [appleOAuthClient, kakaoOAuthClient]
}
```

### 참고 문서

- [Spring Framework - Collection Injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html#beans-collection-injection)
- [Baeldung - Injecting Collections in Spring](https://www.baeldung.com/spring-injecting-collections)
