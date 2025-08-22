package com.runky.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fcm")
public record FirebaseProperties(
	String filePath,
	String projectId
) {
}
