package com.runky.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server()
                .url("https://api.runky.store")
                .description("Runky API");

        return new OpenAPI()
                .addServersItem(server)
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Runky API")
                .description("Runky API 문서")
                .version("1.0.0");
    }
}
