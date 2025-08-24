package com.runky.notification.domain.notification;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.runky.global.error.GlobalException;
import com.runky.notification.error.NotificationErrorCode;

public enum NotificationTemplate {
	// 응원: "${NICKNAME}님이 응원을 보내셨어요!"
	CHEER(
		"응원",
		"${NICKNAME}님이 응원을 보내셨어요!",
		EnumSet.of(VarKey.NICKNAME)
	),

	// 목표 달성
	GOAL_WEEKLY_ACHIEVED(
		"목표 달성",
		"우리 크루, 이번 주도 완주 GO! 크루가 이번 주 목표를 달성 했어요!",
		EnumSet.noneOf(VarKey.class)
	),

	// 크루 새 멤버
	CREW_NEW_MEMBER(
		"크루",
		"우리 크루, 이번 주도 완주 GO! 크루에 새 멤버 ${NICKNAME}님이 들어왔어요.",
		EnumSet.of(VarKey.NICKNAME)
	),

	// 새로운 크루 리더
	CREW_NEW_LEADER(
		"크루",
		"${NICKNAME} 님이 새로운 크루 리더가 되었어요.",
		EnumSet.of(VarKey.NICKNAME)
	),

	// 크루 해체
	CREW_DISBANDED(
		"크루",
		"${CREW_NAME} 크루가 크루 리더에 의해 해체되었어요.",
		EnumSet.of(VarKey.CREW_NAME)
	),

	// 런닝 시작
	RUN_STARTED(
		"런닝",
		"${CREW_NAME}의 ${NICKNAME}님이 런닝을 시작했어요!",
		EnumSet.of(VarKey.CREW_NAME, VarKey.NICKNAME)
	);

	private final String title;
	private final String raw;
	private final Set<VarKey> required;

	NotificationTemplate(String title, String raw, Set<VarKey> required) {
		this.title = title;
		this.raw = raw;
		this.required = required;
	}

	public String title() {
		return title;
	}

	public String raw() {
		return raw;
	}

	public Set<VarKey> required() {
		return required;
	}

	/** FCM/DB 저장용 최종 문장 생성 */
	public String renderText(Map<VarKey, String> vars) {
		return NotificationFormatter.render(this.raw, this.required, vars);
	}

	public enum VarKey {NICKNAME, CREW_NAME}

	public static class NotificationFormatter {
		private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

		public static String render(String rawTemplate, Set<VarKey> requiredKeys, Map<VarKey, String> variables) {
			validateRequiredVariables(requiredKeys, variables);
			return renderTemplateWithRegex(rawTemplate, variables);
		}

		/**
		 * 메시지 생성에 필요한 모든 변수가 제공되었는지 검증
		 */
		private static void validateRequiredVariables(Set<VarKey> requiredKeys, Map<VarKey, String> variables) {
			for (VarKey key : requiredKeys) {
				String value = variables.get(key);
				if (value == null || value.isBlank()) {
					throw new GlobalException(NotificationErrorCode.EMPTY_VAR_KEY_NOTIFICATION_MESSAGE);
				}
			}
		}

		/**
		 * 정규식을 사용하여 템플릿의 플레이스홀더를 실제 값으로 치환
		 */
		private static String renderTemplateWithRegex(String rawTemplate, Map<VarKey, String> variables) {
			Matcher placeholderMatcher = PLACEHOLDER_PATTERN.matcher(rawTemplate);
			StringBuilder resultBuilder = new StringBuilder();

			while (placeholderMatcher.find()) {
				String keyString = placeholderMatcher.group(1);
				VarKey key = VarKey.valueOf(keyString);
				String value = variables.getOrDefault(key, "");
				placeholderMatcher.appendReplacement(resultBuilder, Matcher.quoteReplacement(value));
			}
			placeholderMatcher.appendTail(resultBuilder);

			return resultBuilder.toString();
		}
	}

}
