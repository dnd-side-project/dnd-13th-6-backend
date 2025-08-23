package com.runky.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.BDDMockito.given;

import com.runky.global.error.GlobalException;
import com.runky.member.error.MemberErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;
    @Mock
    private MemberRepository memberRepository;


    @Nested
    @DisplayName("뱃지 변경 시,")
    class ChangeBadge {

        @Test
        @DisplayName("멤버가 존재하지 않으면, MEMBER_NOT_FOUND 예외가 발생한다.")
        void throwMemberNotFoundException_whenMemberNotFound() {
            given(memberRepository.findById(1L))
                    .willReturn(Optional.empty());

            GlobalException exception = assertThrows(GlobalException.class,
                    () -> memberService.changeBadge(new MemberCommand.ChangeBadge(1L, 1L)));

            assertThat(exception)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(MemberErrorCode.MEMBER_NOT_FOUND));
        }
    }
}