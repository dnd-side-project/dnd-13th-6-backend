package com.runky.notification.domain.notification;

import org.springframework.stereotype.Component;

@Component
public class TemplateCatalog {
	public NotificationTemplate resolve(NotificationMessageType type) {
		return switch (type) {
			case CHEER -> NotificationTemplate.CHEER;
			case PERSONAL_GOAL_ACHIEVED -> NotificationTemplate.PERSONAL_GOAL_ACHIEVED;
			case PERSONAL_GOAL_FAILED -> NotificationTemplate.PERSONAL_GOAL_FAILED;
			case CREW_GOAL_ACHIEVED -> NotificationTemplate.CREW_GOAL_ACHIEVED;
			case CREW_GOAL_FAILED -> NotificationTemplate.CREW_GOAL_FAILED;
			case CREW_DISBANDED -> NotificationTemplate.CREW_DISBANDED;
			case RUN_STARTED -> NotificationTemplate.RUN_STARTED;
		};
	}
}
