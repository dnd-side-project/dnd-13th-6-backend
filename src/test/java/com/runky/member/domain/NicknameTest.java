package com.runky.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.runky.global.error.GlobalException;
import com.runky.member.error.MemberErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NicknameTest {

    @Nested
    @DisplayName("닉네임 생성 시,")
    class Create {

        @Test
        @DisplayName("닉네임이 공백일 경우, BLANK_NICKNAME 예외를 발생시킨다.")
        void throwBlankNicknameException_whenBlank() {
            GlobalException exception = assertThrows(GlobalException.class, () -> new Nickname(" "));

            assertThat(exception)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(MemberErrorCode.BLANK_NICKNAME));
        }

        @Test
        @DisplayName("닉네임이 10자를 초과할 경우, OVER_LENGTH_NICKNAME 예외를 발생시킨다.")
        void throwOverLengthNicknameException_whenOverLength() {
            GlobalException exception = assertThrows(GlobalException.class, () -> new Nickname("abcdefghijk"));

            assertThat(exception)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(MemberErrorCode.OVER_LENGTH_NICKNAME));
        }

        @Test
        @DisplayName("닉네임에 특수문자가 포함된 경우, INVALID_FORMAT_NICKNAME 예외를 발생시킨다.")
        void throwInvalidFormatNicknameException_whenIncludeSpecialCharacter() {
            GlobalException exception = assertThrows(GlobalException.class, () -> new Nickname("abcde!@#"));

            assertThat(exception)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(MemberErrorCode.INVALID_FORMAT_NICKNAME));
        }

        @Test
        @DisplayName("닉네임은 한글, 영문 대소문자, 숫자를 포함할 수 있다.")
        void createNickname_whenValid() {
            assertAll(
                    () -> assertDoesNotThrow(() -> new Nickname("가나다라마")),
                    () -> assertDoesNotThrow(() -> new Nickname("abcdeABCDE")),
                    () -> assertDoesNotThrow(() -> new Nickname("1234512345")),
                    () -> assertDoesNotThrow(() -> new Nickname("가나다abc123")),
                    () -> assertDoesNotThrow(() -> new Nickname("가나다ABc123"))
            );
        }
    }
}