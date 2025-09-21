package com.runky.global.security;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.runky.auth.domain.port.TokenDecoder;
import com.runky.global.security.filter.JwtAuthenticationEntryPoint;
import com.runky.global.security.filter.JwtCookieAuthFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		CorsConfigurationSource corsConfigurationSource,
		TokenDecoder tokenDecoder
	) throws Exception {

		return http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.sessionManagement(session -> session.sessionCreationPolicy(
				SessionCreationPolicy.IF_REQUIRED))
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(jwtAuthenticationEntryPoint))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/auth/**")
				.permitAll()
				.requestMatchers("swagger-ui/**", "swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**",
					"/ws.html")
				.permitAll()
				.requestMatchers("/ws/**")
				.permitAll()
				.requestMatchers("/health")
				.permitAll()
				.requestMatchers("/dev/api/running/**", "/dev/api/auth/**")
				.permitAll()
				.anyRequest()
				.authenticated()
			)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.addFilterBefore(new JwtCookieAuthFilter(tokenDecoder), UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(Arrays.asList(
			"https://*.runky.store", "http://*.runky.store",
			"https://localhost:3000", "http://localhost:3000"
		));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		configuration.setAllowedHeaders(Collections.singletonList("*")); // 모든 헤더 허용
		configuration.setAllowCredentials(true); // 인증 정보를 포함한 요청 허용
		configuration.setMaxAge(3600L); // 캐싱 시간 설정

		configuration.setExposedHeaders(Arrays.asList(
			"X-Access-Token", "X-Refresh-Token", "X-Signup-Token", "Authorization"
		));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
