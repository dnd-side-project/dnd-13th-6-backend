package com.dnd13.runners_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class RunnersServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RunnersServerApplication.class, args);
	}

}
