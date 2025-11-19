package com.runky.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.runky.auth.infrastructure.erternal.AppleApiHttpClient;
import com.runky.auth.infrastructure.erternal.KakaoApiHttpClient;

@Configuration
public class HttpInterfaceConfig {

	@Bean
	public KakaoApiHttpClient kakaoApiHttpClient() {
		RestClient restClient = RestClient.builder().build();

		HttpServiceProxyFactory factory = HttpServiceProxyFactory
			.builderFor(RestClientAdapter.create(restClient))
			.build();

		return factory.createClient(KakaoApiHttpClient.class);
	}

	@Bean
	public AppleApiHttpClient appleApiHttpClient() {
		RestClient restClient = RestClient.builder().build();

		HttpServiceProxyFactory factory = HttpServiceProxyFactory
			.builderFor(RestClientAdapter.create(restClient))
			.build();

		return factory.createClient(AppleApiHttpClient.class);
	}
}
