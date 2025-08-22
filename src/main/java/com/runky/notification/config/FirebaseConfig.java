package com.runky.notification.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

	//TODO: 수정필요. 현재 json값은 운영값이 아닌 테스트값
	@PostConstruct
	public void init() throws IOException {
		GoogleCredentials credentials = GoogleCredentials
			.fromStream(new ClassPathResource("/firebase/runky_test_fcm.json").getInputStream());
		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(credentials)
			.build();

		if (FirebaseApp.getApps().isEmpty()) {
			FirebaseApp.initializeApp(options);
		}
	}

	@Bean
	public FirebaseMessaging firebaseMessaging() {
		return FirebaseMessaging.getInstance();
	}
}
