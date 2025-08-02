package com.dnd13.runners_server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * 도메인:포트+프리픽스/swagger-ui/index.html#/
 * ex) http://localhost:8080/swagger-ui/index.html#/
 */
@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.components(new Components())
			.info(apiInfo());
	}
	/**
	 * Swagger에서 보여줄 API 정보를 설정
	 */
	private Info apiInfo() {
		return new Info()
			.title("CodeArena Swagger")
			.description("CodeArena 유저 및 인증 , ps, 알림에 관한 REST API")
			.version("1.0.0");
	}
}
