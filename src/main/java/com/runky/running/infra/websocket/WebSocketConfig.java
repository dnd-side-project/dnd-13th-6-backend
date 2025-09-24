package com.runky.running.infra.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.runky.running.infra.websocket.exception.StompErrorFrameHandler;
import com.runky.running.infra.websocket.handshake.CookieAuthHandshakeInterceptor;
import com.runky.running.infra.websocket.inbound.InboundChannelLogger;
import com.runky.running.infra.websocket.inbound.JwtChannelInterceptor;
import com.runky.running.infra.websocket.outbound.OutboundChannelLogger;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final JwtChannelInterceptor jwtChannelInterceptor;
	private final InboundChannelLogger inboundChannelLogger;
	private final OutboundChannelLogger outboundChannelLogger;
	private final CookieAuthHandshakeInterceptor cookieAuthHandshakeInterceptor;

	private final StompErrorFrameHandler stompErrorFrameHandler;

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
		registry.setErrorHandler(stompErrorFrameHandler);

	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration reg) {
		reg.interceptors(jwtChannelInterceptor, inboundChannelLogger);
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration reg) {
		reg.interceptors(outboundChannelLogger);
	}

}
