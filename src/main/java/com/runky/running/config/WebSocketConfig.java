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
	private final JwtChannelInterceptor jwtChannelInterceptor;
	private final StompInboundSendLogger stompInboundSendLogger;
	private final StompOutboundMessageLogger stompOutboundMessageLogger;
	private final CookieAuthHandshakeInterceptor cookieAuthHandshakeInterceptor;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/app");
		registry.enableSimpleBroker("/topic", "/queue");
		registry.setUserDestinationPrefix("/user");

	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry
			.addEndpoint("/ws")
			.addInterceptors(cookieAuthHandshakeInterceptor)
			.setAllowedOriginPatterns(
				"https://*.runky.store", "http://*.runky.store",
				"https://localhost:*", "http://localhost:*",
				"null"
			);
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration reg) {
		reg.interceptors(jwtChannelInterceptor, stompInboundSendLogger);
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration reg) {
		reg.interceptors(stompOutboundMessageLogger);
	}

}
