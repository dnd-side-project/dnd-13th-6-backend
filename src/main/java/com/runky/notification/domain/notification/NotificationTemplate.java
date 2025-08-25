package com.runky.notification.domain.notification;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.runky.global.error.GlobalException;
import com.runky.notification.error.NotificationErrorCode;

public enum NotificationTemplate {
	// 런닝 행운
	CHEER(
		"행운 보내기",
		"${NICKNAME}님이 행운을 보냈어요!🍀",
		EnumSet.of(VarKey.NICKNAME)
	),

	// 개인 목표
	PERSONAL_GOAL_ACHIEVED(
		"개인 목표 달성 성공",
		"이번 주 ${KM} 목표를 달성 했어요! 행운이 도착했어요.✨",
		EnumSet.of(VarKey.KM)
	),
	PERSONAL_GOAL_FAILED(
		"개인 목표 달성 실패",
		"클로버는 놓쳤지만, 꾸준함이 곧 행운이에요.🌱",
		EnumSet.noneOf(VarKey.class)
	),

	// 크루 목표
	CREW_GOAL_ACHIEVED(
		"크루 목표 달성 성공",
		"이번주 크루 목표를 확인해보세요.🎉",
		EnumSet.noneOf(VarKey.class)
	),
	CREW_GOAL_FAILED(
		"크루 목표 달성 실패",
		"이번엔 놓쳤지만, 꾸준함이 곧 행운이에요.🌱",
		EnumSet.noneOf(VarKey.class)
	),

	// 크루 해체
	CREW_DISBANDED(
		"크루",
		"${CREW_NAME이 크루 리더의 결정으로 해체되었어요.",
		EnumSet.of(VarKey.CREW_NAME)
	),

	// 런닝 시작
	RUN_STARTED(
		"런닝 시작",
		"${NICKNAME}님이 런닝을 시작했어요!🏃🏻",
		EnumSet.of(VarKey.NICKNAME)
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

	public enum VarKey {NICKNAME, CREW_NAME, KM}

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
