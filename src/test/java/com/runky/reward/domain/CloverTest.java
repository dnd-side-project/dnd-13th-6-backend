package com.runky.reward.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.runky.global.error.GlobalException;
import com.runky.reward.error.RewardErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CloverTest {

    @Test
    @DisplayName("클로버 추가 시, 사용 값이 음수이면, INVALID_CLOVER_ADD_REQUEST 예외가 발생한다.")
    void throwException_whenAddMinus() {
        Clover clover = Clover.of(1L);

        GlobalException thrown = assertThrows(GlobalException.class, () -> clover.add(-1L));

        assertThat(thrown)
                .usingRecursiveComparison()
                .isEqualTo(new GlobalException(RewardErrorCode.INVALID_CLOVER_ADD_REQUEST));
    }

    @Test
    @DisplayName("클로버 사용시 시, 보유 수량이 사용 수량보다 적으면, INSUFFICIENT_CLOVER 예외가 발생한다.")
    void throwException_whenUseMoreThanCount() {
        Clover clover = Clover.of(1L);
        clover.add(10L);

        GlobalException thrown = assertThrows(GlobalException.class, () -> clover.use(11L));

        assertThat(thrown)
                .usingRecursiveComparison()
                .isEqualTo(new GlobalException(RewardErrorCode.INSUFFICIENT_CLOVER));
    }
}