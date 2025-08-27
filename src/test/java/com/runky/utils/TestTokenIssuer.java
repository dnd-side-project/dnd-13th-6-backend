package com.runky.utils;

import com.runky.auth.domain.port.TokenIssuer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class TestTokenIssuer {

    private final TokenIssuer tokenIssuer;

    public TestTokenIssuer(TokenIssuer tokenIssuer) {
        this.tokenIssuer = tokenIssuer;
    }

    public HttpHeaders issue(long memberId, String role) {
        var issued = tokenIssuer.issue(memberId, role);
        String accessToken = issued.access().token();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "accessToken=" + accessToken);
        return headers;
    }
}
