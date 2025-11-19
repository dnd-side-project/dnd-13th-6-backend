package com.runky.auth.infrastructure.erternal;

import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.runky.auth.infrastructure.erternal.dto.ApplePublicKeys;
import com.runky.auth.infrastructure.erternal.dto.AppleTokenResponse;

public interface AppleApiHttpClient {

	@PostExchange(url = "https://appleid.apple.com/auth/token", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	AppleTokenResponse getAccessToken(@RequestBody MultiValueMap<String, String> body);

	@GetExchange("https://appleid.apple.com/auth/keys")
	ApplePublicKeys getPublicKeys();
}
