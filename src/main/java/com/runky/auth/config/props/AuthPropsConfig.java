package com.runky.auth.config.props;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
	AuthCookieProperties.class,
	JwtProperties.class,
	SignupTokenProperties.class,
	RefreshTokenHashProperties.class,
	KakaoProperties.class,
	LoginRedirectProperties.class
})
public class AuthPropsConfig {
}
