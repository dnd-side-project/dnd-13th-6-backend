package com.runky.reward.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.runky.global.error.GlobalException;
import com.runky.reward.error.RewardErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class BadgeTest {

    @Nested
    @DisplayName("배지 생성 시,")
    class Create {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("이미지 URL이 비어있으면 예외가 발생한다.")
        void invalidImageUrl(String url) {
            String name = "Test Badge";

            Exception exception = assertThrows(GlobalException.class, () -> Badge.of(url, name));

            assertThat(exception)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(RewardErrorCode.INVALID_BADGE_IMAGE_URL));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("이름이 비어있으면 예외가 발생한다.")
        void invalidName(String name) {
            String imageUrl = "http://example.com/image.png";

            Exception exception = assertThrows(GlobalException.class, () -> Badge.of(imageUrl, name));

            assertThat(exception)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(RewardErrorCode.INVALID_BADGE_NAME));
        }
    }
}