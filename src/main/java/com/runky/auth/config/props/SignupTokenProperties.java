package com.runky.auth.config.props;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.Min;

@ConfigurationProperties(prefix = "runky.security.signup-token")
public record SignupTokenProperties(@Min(1) Long ttlMinutes) {
	public Duration ttl() {
		return Duration.ofMinutes(ttlMinutes);
	}
}
