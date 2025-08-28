package com.runky.auth.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "runky.login.redirect")
public record LoginRedirectProperties(
	String newUser,
	String alreadyExistingUser
) {
}
