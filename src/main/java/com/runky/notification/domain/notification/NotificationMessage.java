package com.runky.notification.domain.notification;

import static com.runky.notification.domain.notification.NotificationMessage.*;
import static com.runky.notification.domain.notification.NotificationMessageType.*;
import static com.runky.notification.domain.notification.NotificationTemplate.VarKey.*;

import java.util.Map;

import com.runky.notification.domain.notification.NotificationTemplate.VarKey;

public sealed interface NotificationMessage
	permits Cheer,
	GoalWeeklyAchieved,
	CrewNewMember,
	CrewNewLeader,
	CrewDisbanded,
	RunStarted {

	/** 알림의 의미/종류 */
	NotificationMessageType type();

	/** 템플릿 치환에 사용할 변수 집합 */
	Map<VarKey, String> variables();

	/** 응원 알림 */
	record Cheer(Nickname nickname) implements NotificationMessage {
		@Override
		public NotificationMessageType type() {
			return CHEER;
		}

		@Override
		public Map<VarKey, String> variables() {
			return Map.of(NICKNAME, nickname.value());
		}
	}

	/** 주간 목표 달성 알림 */
	record GoalWeeklyAchieved() implements NotificationMessage {
		@Override
		public NotificationMessageType type() {
			return GOAL_WEEKLY_ACHIEVED;
		}

		@Override
		public Map<VarKey, String> variables() {
			return Map.of();
		}
	}

	/** 크루 신규 멤버 알림 */
	record CrewNewMember(Nickname nickname) implements NotificationMessage {
		@Override
		public NotificationMessageType type() {
			return CREW_NEW_MEMBER;
		}

		@Override
		public Map<VarKey, String> variables() {
			return Map.of(NICKNAME, nickname.value());
		}
	}

	/** 새로운 크루 리더 알림 */
	record CrewNewLeader(Nickname nickname) implements NotificationMessage {
		@Override
		public NotificationMessageType type() {
			return CREW_NEW_LEADER;
		}

		@Override
		public Map<VarKey, String> variables() {
			return Map.of(NICKNAME, nickname.value());
		}
	}

	/** 크루 해체 알림 */
	record CrewDisbanded(CrewName crewName) implements NotificationMessage {
		@Override
		public NotificationMessageType type() {
			return CREW_DISBANDED;
		}

		@Override
		public Map<VarKey, String> variables() {
			return Map.of(CREW_NAME, crewName.value());
		}
	}

	/** 런닝 시작 알림 */
	record RunStarted(CrewName crewName, Nickname nickname) implements NotificationMessage {
		@Override
		public NotificationMessageType type() {
			return RUN_STARTED;
		}

		@Override
		public Map<VarKey, String> variables() {
			return Map.of(CREW_NAME, crewName.value(), NICKNAME, nickname.value());
		}
	}
}
