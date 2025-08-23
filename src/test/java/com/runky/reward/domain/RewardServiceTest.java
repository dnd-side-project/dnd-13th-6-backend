package com.runky.reward.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.runky.global.error.GlobalException;
import com.runky.reward.error.RewardErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RewardServiceTest {

    @InjectMocks
    private RewardService rewardService;
    @Mock
    private BadgeRepository badgeRepository;
    @Mock
    private CloverRepository cloverRepository;

    @Test
    @DisplayName("클로버 조회 시, 사용자 클로버 정보가 없으면, NOT_FOUND_CLOVER 예외가 발생한다.")
    void throwNotFoundCloverException_whenCloverNotFound() {
        given(cloverRepository.findByUserId(1L))
                .willReturn(Optional.empty());

        RewardCommand.GetClover command = new RewardCommand.GetClover(1L);

        GlobalException exception = assertThrows(GlobalException.class, () -> rewardService.getClover(command));

        assertThat(exception)
                .usingRecursiveComparison()
                .isEqualTo(new GlobalException(RewardErrorCode.NOT_FOUND_CLOVER));
    }

    @Test
    @DisplayName("뱃지 조회 시, 뱃지가 없으면, NOT_FOUND_BADGE 예외가 발생한다.")
    void throwNotFoundBadgeException_whenBadgeNotFound() {
        given(badgeRepository.findBadge(1L))
                .willReturn(Optional.empty());

        RewardCommand.Find command = new RewardCommand.Find(1L);

        GlobalException exception = assertThrows(GlobalException.class, () -> rewardService.getBadge(command));

        assertThat(exception)
                .usingRecursiveComparison()
                .isEqualTo(new GlobalException(RewardErrorCode.NOT_FOUND_BADGE));
    }
}