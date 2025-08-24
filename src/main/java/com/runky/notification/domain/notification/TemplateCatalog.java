package com.runky.notification.domain.notification;

import org.springframework.stereotype.Component;

@Component
public class TemplateCatalog {
	public NotificationTemplate resolve(NotificationMessageType type) {
		return switch (type) {
			case CHEER -> NotificationTemplate.CHEER;
			case GOAL_WEEKLY_ACHIEVED -> NotificationTemplate.GOAL_WEEKLY_ACHIEVED;
			case CREW_NEW_MEMBER -> NotificationTemplate.CREW_NEW_MEMBER;
			case CREW_NEW_LEADER -> NotificationTemplate.CREW_NEW_LEADER;
			case CREW_DISBANDED -> NotificationTemplate.CREW_DISBANDED;
			case RUN_STARTED -> NotificationTemplate.RUN_STARTED;
		};
	}
}
