package com.runky.auth.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apple")
public record AppleProperties(
	String serviceId,
	String teamId,
	String keyId,
	String privateKey,
	String redirectUrl
) {
}
