package com.runky.notification.domain.push;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DeviceTokenTest {

	@Nested
	@DisplayName("DeviceToken.register()")
	class RegisterDeviceToken {
		@Test
		@DisplayName("memberId, token으로 active=true 상태의 토큰을 생성한다.")
		void createActiveToken() {
			// when
			DeviceToken token = DeviceToken.register(1L, "tkn-123", "MOBILE");

			// then
			assertThat(token.getId()).isNull(); // 영속 전이므로 null
			assertThat(token.getMemberId()).isEqualTo(1L);
			assertThat(token.getToken()).isEqualTo("tkn-123");
			assertThat(token.isActive()).isTrue();
		}
	}

	@Nested
	@DisplayName("활성/비활성 전환")
	class Toggle {
		@Test
		@DisplayName("deactivate() 호출 시 active=false 가 된다.")
		void deactivate() {
			DeviceToken token = DeviceToken.register(1L, "tkn-123", "MOBILE");

			token.deactivate();

			assertThat(token.isActive()).isFalse();
		}

		@Test
		@DisplayName("reactivate() 호출 시 active=true 가 된다.")
		void reactivate() {
			DeviceToken token = DeviceToken.register(1L, "tkn-123", "MOBILE");
			token.deactivate();

			token.reactivate();

			assertThat(token.isActive()).isTrue();
		}
	}
}
