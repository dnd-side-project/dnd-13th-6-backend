package com.runky.running.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
	private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/app"); // 클라이언트가 메시지를 보낼 때 접두사
		registry.enableSimpleBroker("/topic", "/queue"); // 클라이언트가 구독할 주소
		registry.setUserDestinationPrefix("/user");

	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry
			.addEndpoint("/ws")
			.addInterceptors(jwtHandshakeInterceptor)
			.setAllowedOriginPatterns(
				"https://web.runky.store", "http://web.runky.store",
				"https://localhost:3000", "http://localhost:3000"
			)
			.withSockJS();
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		// 인터셉터 등록
		registration.interceptors(stompAuthChannelInterceptor);
	}
}
