package com.runky.notification.domain.notification;

import static com.runky.notification.domain.notification.NotificationMessageType.*;
import static com.runky.notification.domain.notification.NotificationTemplate.VarKey.*;

import java.util.Map;

public sealed interface NotificationMessage {

	/** 알림의 의미/종류 */
	NotificationMessageType type();

	/** 템플릿 치환에 사용할 변수 집합 */
	Map<NotificationTemplate.VarKey, String> variables();

	/** 응원 알림 */
	record Cheer(Nickname nickname) implements NotificationMessage {
		@Override
		public NotificationMessageType type() {
			return CHEER;
		}

		@Override
		public Map<NotificationTemplate.VarKey, String> variables() {
			return Map.of(NICKNAME, nickname.value());
		}
	}

	/** 개인 목표 달성 알림 */
	record PersonalGoalAchieved() implements NotificationMessage {
		@Override
		public NotificationMessageType type() {
			return PERSONAL_GOAL_ACHIEVED;
		}

		@Override
		public Map<NotificationTemplate.VarKey, String> variables() {
			return Map.of();
		}
	}

	/** 개인 목표 실패 알림 */
	record PersonalGoalFailed() implements NotificationMessage {
		@Override
		public NotificationMessageType type() {
			return PERSONAL_GOAL_FAILED;
		}

		@Override
		public Map<NotificationTemplate.VarKey, String> variables() {
			return Map.of();
		}
	}

	/** 크루 묵표 달성 알림 */
	record CrewGoalAchieved() implements NotificationMessage {
		@Override
		public NotificationMessageType type() {
			return CREW_GOAL_ACHIEVED;
		}

		@Override
		public Map<NotificationTemplate.VarKey, String> variables() {
			return Map.of();
		}

	}

	/** 크루 묵표 실패 알림 */

	record CrewGoalFailed() implements NotificationMessage {
		@Override
		public NotificationMessageType type() {
			return CREW_GOAL_FAILED;
		}

		@Override
		public Map<NotificationTemplate.VarKey, String> variables() {
			return Map.of();
		}
	}

	/** 크루 해체 알림 */
	record CrewDisbanded(CrewName crewName) implements NotificationMessage {
		@Override
		public NotificationMessageType type() {
			return CREW_DISBANDED;
		}

		@Override
		public Map<NotificationTemplate.VarKey, String> variables() {
			return Map.of(CREW_NAME, crewName.value());
		}
	}

	/** 런닝 시작 알림 */
	record RunStarted(Nickname nickname) implements NotificationMessage {
		@Override
		public NotificationMessageType type() {
			return RUN_STARTED;
		}

		@Override
		public Map<NotificationTemplate.VarKey, String> variables() {
			return Map.of(NICKNAME, nickname.value());
		}
	}
}
