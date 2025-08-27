package com.runky.crew.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.runky.crew.domain.Code;
import com.runky.crew.domain.Crew;
import com.runky.crew.domain.CrewCommand;
import com.runky.crew.domain.CrewRepository;
import com.runky.goal.domain.GoalRepository;
import com.runky.member.domain.ExternalAccount;
import com.runky.member.domain.Member;
import com.runky.member.domain.MemberRepository;
import com.runky.reward.domain.Badge;
import com.runky.reward.domain.BadgeRepository;
import com.runky.running.domain.Running;
import com.runky.running.domain.RunningRepository;
import com.runky.utils.DatabaseCleanUp;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CrewFacadeIntegrationTest {

    @Autowired
    private CrewFacade crewFacade;
    @Autowired
    private CrewRepository crewRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BadgeRepository badgeRepository;
    @Autowired
    private RunningRepository runningRepository;
    @Autowired
    private GoalRepository goalRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("크루 목록 조회 시,")
    class GetCrews {

        @Test
        @DisplayName("크루에 속한 멤버들의 각 이미지를 함께 반환한다.")
        void returnMemberBadgeImages() {
            Member saveMember1 = memberRepository.save(Member.register(ExternalAccount.of("kakao", "id1"), "name1"));
            Member member2 = Member.register(ExternalAccount.of("kakao", "id2"), "name2");
            member2.changeBadge(2L);
            Member saveMember2 = memberRepository.save(member2);
            Member member3 = Member.register(ExternalAccount.of("kakao", "id3"), "name3");
            member3.changeBadge(3L);
            Member saveMember3 = memberRepository.save(member3);
            Member member4 = Member.register(ExternalAccount.of("kakao", "id4"), "name4");
            member4.changeBadge(4L);
            Member saveMember4 = memberRepository.save(member4);
            Running running1 = Running.builder()
                    .runnerId(saveMember2.getId())
                    .status(Running.Status.FINISHED)
                    .startedAt(LocalDateTime.now().minusHours(1))
                    .endedAt(LocalDateTime.now())
                    .totalDistanceMeter(10000.0)
                    .durationSeconds(3600L)
                    .avgSpeedMPS(2.5)
                    .build();
            runningRepository.save(running1);

            Crew crew1 = Crew.of(new CrewCommand.Create(saveMember1.getId(), "crew1"), new Code("abc123"));
            crew1.joinMember(saveMember2.getId());
            crewRepository.save(crew1);
            Crew crew2 = Crew.of(new CrewCommand.Create(saveMember2.getId(), "crew2"), new Code("abc124"));
            crew2.joinMember(saveMember1.getId());
            crew2.joinMember(saveMember3.getId());
            crew2.joinMember(saveMember4.getId());
            crew2.leaveMember(saveMember4.getId());
            crewRepository.save(crew2);

            Running running2 = Running.builder()
                    .runnerId(saveMember3.getId())
                    .status(Running.Status.RUNNING)
                    .startedAt(LocalDateTime.now().minusHours(1))
                    .build();
            runningRepository.save(running2);

            badgeRepository.save(Badge.of("/badge1.png", "badge 1"));
            badgeRepository.save(Badge.of("/badge2.png", "badge 2"));
            badgeRepository.save(Badge.of("/badge3.png", "badge 3"));
            badgeRepository.save(Badge.of("/badge4.png", "badge 4"));

            List<CrewResult.Card> cards = crewFacade.getCrews(1L);

            assertThat(cards.size()).isEqualTo(2);
            assertThat(cards)
                    .extracting("badgeImageUrls")
                    .containsExactlyInAnyOrder(
                            List.of("/badge1.png", "/badge2.png"),
                            List.of("/badge1.png", "/badge2.png", "/badge3.png")
                    );
            assertThat(cards)
                    .extracting("memberCount")
                    .containsExactlyInAnyOrder(2L, 3L);
            assertThat(cards)
                    .extracting("isLeader")
                    .containsExactlyInAnyOrder(true, false);
            assertThat(cards)
                    .extracting("crewName")
                    .containsExactlyInAnyOrder("crew1", "crew2");
            assertThat(cards)
                    .extracting("crewId")
                    .containsExactlyInAnyOrder(crew1.getId(), crew2.getId());
            assertThat(cards)
                    .extracting("isRunning")
                    .containsExactlyInAnyOrder(false, true);
        }
    }

    @Nested
    @DisplayName("크루 상세 조회 시,")
    class GetCrewDetail {
        @Test
        @DisplayName("크루에 대한 상세 정보를 반환한다.")
        void returnCrewDetail() {
            Crew crew = Crew.of(new CrewCommand.Create(1L, "crew 1"), new Code("abc123"));
            crew.joinMember(2L);
            crew.joinMember(3L);
            crew.leaveMember(3L);
            crew.joinMember(4L);
            crew.banMember(4L);
            crewRepository.save(crew);
        }
    }
}