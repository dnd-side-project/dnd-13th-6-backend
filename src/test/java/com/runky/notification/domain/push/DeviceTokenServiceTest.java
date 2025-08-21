package com.runky.notification.domain.push;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.dao.DataIntegrityViolationException;

import com.runky.global.error.GlobalException;
import com.runky.notification.error.NotificationErrorCode;

@ExtendWith(MockitoExtension.class)
@MockitoSettings
class DeviceTokenServiceTest {

	@Mock
	DeviceTokenRepository deviceTokenRepository;

	@InjectMocks
	DeviceTokenService deviceTokenService;

	@Nested
	@DisplayName("register()")
	class Register {
		@Test
		@DisplayName("정상 등록 시 Repository.save 가 호출된다.")
		void saveOnce() {
			// given
			when(deviceTokenRepository.save(any(DeviceToken.class)))
				.thenAnswer(inv -> inv.getArgument(0));

			// when
			deviceTokenService.register(new DeviceTokenCommand.Register(1L, "tkn-123", "MOBILE"));

			// then
			verify(deviceTokenRepository)
				.save(argThat(dt -> dt.getMemberId().equals(1L)
					&& dt.getToken().equals("tkn-123")
					&& dt.isActive()));
		}

		@Test
		@DisplayName("토큰 유니크 키 충돌 시, GlobalException(DUPLICATE_UNIQUE_KEY_DEVICE_TOKEN)을 던진다.")
		void duplicateTokenToGlobalException() {
			// given: 서비스는 제약조건명 'ux_device_token_token' 포함 메시지를 매핑 기준으로 사용
			when(deviceTokenRepository.save(any(DeviceToken.class)))
				.thenThrow(new DataIntegrityViolationException("... ux_device_token_token ..."));

			// when
			GlobalException thrown = assertThrows(GlobalException.class,
				() -> deviceTokenService.register(new DeviceTokenCommand.Register(1L, "dup-token", "MOBILE")));

			// then
			assertThat(thrown)
				.usingRecursiveComparison()
				.isEqualTo(new GlobalException(NotificationErrorCode.DUPLICATE_UNIQUE_KEY_DEVICE_TOKEN));
		}
	}

	@Nested
	@DisplayName("delete()")
	class Delete {
		@Test
		@DisplayName("삭제 대상이 있으면 삭제 개수를 반환한다.")
		void deleteExisting() {
			// given
			when(deviceTokenRepository.deleteByMemberIdAndToken(1L, "tkn-123")).thenReturn(1);

			// when
			DeviceTokenInfo.Delete result =
				deviceTokenService.delete(new DeviceTokenCommand.Delete(1L, "tkn-123"));

			// then
			assertThat(result.count()).isEqualTo(1);
		}

		@Test
		@DisplayName("삭제 대상이 없으면 GlobalException(NOT_EXIST_TO_DELETE_DEVICE_TOKEN)을 던진다.")
		void deleteNotFound() {
			// given
			when(deviceTokenRepository.deleteByMemberIdAndToken(1L, "nope")).thenReturn(0);

			// when
			GlobalException thrown = assertThrows(GlobalException.class,
				() -> deviceTokenService.delete(new DeviceTokenCommand.Delete(1L, "nope")));

			// then
			assertThat(thrown)
				.usingRecursiveComparison()
				.isEqualTo(new GlobalException(NotificationErrorCode.NOT_EXIST_TO_DELETE_DEVICE_TOKEN));
		}
	}

	@Nested
	@DisplayName("view()")
	class View {
		@Test
		@DisplayName("memberId+token으로 조회에 성공하면 View DTO를 반환한다.")
		void viewFound() {
			// given
			DeviceToken entity = DeviceToken.builder()
				.id(10L).memberId(1L).token("tkn-123").active(true).build();

			when(deviceTokenRepository.findByMemberIdAndToken(1L, "tkn-123"))
				.thenReturn(Optional.of(entity));

			// when
			DeviceTokenInfo.View view =
				deviceTokenService.getDeviceToken(new DeviceTokenCommand.Find(1L, "tkn-123"));

			// then
			assertThat(view.id()).isEqualTo(10L);
			assertThat(view.memberId()).isEqualTo(1L);
			assertThat(view.token()).isEqualTo("tkn-123");
			assertThat(view.active()).isTrue();
		}

		@Test
		@DisplayName("조회 대상이 없으면 GlobalException(NOT_FOUND_DEVICE_TOKEN)을 던진다.")
		void viewNotFound() {
			when(deviceTokenRepository.findByMemberIdAndToken(1L, "nope"))
				.thenReturn(Optional.empty());

			GlobalException thrown = assertThrows(GlobalException.class,
				() -> deviceTokenService.getDeviceToken(new DeviceTokenCommand.Find(1L, "nope")));

			assertThat(thrown)
				.usingRecursiveComparison()
				.isEqualTo(new GlobalException(NotificationErrorCode.NOT_FOUND_DEVICE_TOKEN));
		}
	}

	@Nested
	@DisplayName("isExists()")
	class IsExists {
		@Test
		@DisplayName("활성 토큰 존재 여부를 반환한다(존재)")
		void existsTrue() {
			when(deviceTokenRepository.existsActiveByMemberIdAndToken(1L, "tkn-123"))
				.thenReturn(true);

			DeviceTokenInfo.Existence existence =
				deviceTokenService.isExists(new DeviceTokenCommand.Find(1L, "tkn-123"));

			assertThat(existence.exists()).isTrue();
		}

		@Test
		@DisplayName("활성 토큰 존재 여부를 반환한다(부재)")
		void existsFalse() {
			when(deviceTokenRepository.existsActiveByMemberIdAndToken(1L, "tkn-123"))
				.thenReturn(false);

			DeviceTokenInfo.Existence existence =
				deviceTokenService.isExists(new DeviceTokenCommand.Find(1L, "tkn-123"));

			assertThat(existence.exists()).isFalse();
		}
	}
}
