package com.runky.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.runky.global.error.GlobalException;
import com.runky.member.error.MemberErrorCode;
import com.runky.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MemberServiceIntegrationTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("닉네임 변경 시,")
    class ChangeNickname {
        @Test
        @DisplayName("이미 사용중인 닉네임일 경우, DUPLICATE_NICKNAME 예외를 발생시킨다.")
        void throwDuplicateNicknameException_whenNicknameIsAlreadyInUse() {
            Member member1 = memberRepository.save(Member.register(ExternalAccount.of("kakao", "1234"), "nick1"));
            Member member2 = memberRepository.save(Member.register(ExternalAccount.of("kakao", "4321"), "nick2"));
            MemberCommand.ChangeNickname command = new MemberCommand.ChangeNickname(
                    member2.getId(),
                    member1.getNickname().value()
            );

            GlobalException thrown = assertThrows(GlobalException.class, () -> memberService.changeNickname(command));

            assertThat(thrown)
                    .usingRecursiveComparison()
                    .isEqualTo(new GlobalException(MemberErrorCode.DUPLICATE_NICKNAME));
        }
    }
}