package com.runky.notification.domain.notification;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.runky.global.error.GlobalException;

class NotificationTemplateTest {

	@Test
	@DisplayName("성공: 단일 변수 템플릿이 정상적으로 렌더링된다")
	void renderText_WithSinglePlaceholder_Success() {
		// given
		NotificationTemplate template = NotificationTemplate.CHEER;
		Map<NotificationTemplate.VarKey, String> variables = Map.of(NotificationTemplate.VarKey.NICKNAME, "인생한접시");
		String expected = "인생한접시님이 응원을 보내셨어요!";

		// when
		String actual = template.renderText(variables);

		// then
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("성공: 다중 변수 템플릿이 정상적으로 렌더링된다")
	void renderText_WithMultiplePlaceholders_Success() {
		// given
		NotificationTemplate template = NotificationTemplate.RUN_STARTED;
		Map<NotificationTemplate.VarKey, String> variables = Map.of(
			NotificationTemplate.VarKey.CREW_NAME, "6조",
			NotificationTemplate.VarKey.NICKNAME, "김런키"
		);
		String expected = "6조의 김런키님이 런닝을 시작했어요!";

		// when
		String actual = template.renderText(variables);

		// then
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("성공: 변수가 없는 템플릿도 정상적으로 처리된다")
	void renderText_WithNoPlaceholders_Success() {
		// given
		NotificationTemplate template = NotificationTemplate.GOAL_WEEKLY_ACHIEVED;
		Map<NotificationTemplate.VarKey, String> variables = Map.of();
		String expected = "우리 크루, 이번 주도 완주 GO! 크루가 이번 주 목표를 달성 했어요!";

		// when
		String actual = template.renderText(variables);

		// then
		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("예외: 필수 변수가 누락되면 GlobalException이 발생한다")
	void renderText_WhenRequiredVariableIsMissing_ThrowsException() {
		// given
		NotificationTemplate template = NotificationTemplate.CHEER;
		Map<NotificationTemplate.VarKey, String> variables = Map.of();

		// expect
		assertThrows(GlobalException.class, () -> {
			template.renderText(variables);
		});
	}

	@Test
	@DisplayName("예외: 필수 변수가 비어있으면(blank) GlobalException이 발생한다")
	void renderText_WhenRequiredVariableIsBlank_ThrowsException() {
		// given
		NotificationTemplate template = NotificationTemplate.CREW_DISBANDED;
		Map<NotificationTemplate.VarKey, String> variables = Map.of(NotificationTemplate.VarKey.CREW_NAME,
			"   ");

		// expect
		assertThrows(GlobalException.class, () -> {
			template.renderText(variables);
		});
	}
}
