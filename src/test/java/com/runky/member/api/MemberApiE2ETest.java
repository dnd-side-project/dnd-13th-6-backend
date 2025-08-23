package com.runky.member.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.runky.global.response.ApiResponse;
import com.runky.member.domain.ExternalAccount;
import com.runky.member.domain.Member;
import com.runky.member.domain.MemberRepository;
import com.runky.reward.domain.Badge;
import com.runky.reward.domain.BadgeRepository;
import com.runky.reward.domain.MemberBadge;
import com.runky.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BadgeRepository badgeRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    @DisplayName("GET /api/members/me")
    class Get {

        private final String BASE_URL = "/api/members/me";

        @Test
        @DisplayName("유저의 정보를 조회한다.")
        void getMember() {
            Member member = memberRepository.save(Member.register(ExternalAccount.of("kakao", "1234"), "nick"));
            Badge badge = badgeRepository.save(Badge.of("뱃지1", "image1"));
            member.changeBadge(badge.getId());

            ParameterizedTypeReference<ApiResponse<MemberResponse.Detail>> responseType = new ParameterizedTypeReference<>() {
            };
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("X-USER-ID", member.getId().toString());

            ResponseEntity<ApiResponse<MemberResponse.Detail>> response = testRestTemplate.exchange(BASE_URL,
                    HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

            assertThat(response.getBody().getResult().id()).isEqualTo(member.getId());
            assertThat(response.getBody().getResult().nickname()).isEqualTo(member.getNickname().value());
            assertThat(response.getBody().getResult().badgeUrl()).isEqualTo(badge.getImageUrl());
        }
    }

    @Nested
    @DisplayName("PATCH /api/members/me/nickname")
    class ChangeNickname {
        @Test
        @DisplayName("유저의 닉네임을 변경한다.")
        void changeNickname() {
            Member member = memberRepository.save(Member.register(ExternalAccount.of("kakao", "1234"), "nick"));

            ParameterizedTypeReference<ApiResponse<MemberResponse.Nickname>> responseType = new ParameterizedTypeReference<>() {
            };
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("X-USER-ID", member.getId().toString());
            MemberRequest.Nickname request = new MemberRequest.Nickname("newNick");

            ResponseEntity<ApiResponse<MemberResponse.Nickname>> response = testRestTemplate.exchange(
                    "/api/members/me/nickname",
                    HttpMethod.PATCH, new HttpEntity<>(request, httpHeaders), responseType);

            assertThat(response.getBody().getResult().id()).isEqualTo(member.getId());
            assertThat(response.getBody().getResult().nickname()).isEqualTo("newNick");
        }
    }

    @Nested
    @DisplayName("PATCH /api/members/me/badge")
    class ChangeBadge {

        @Test
        @DisplayName("유저의 뱃지를 변경한다.")
        void changeBadge() {
            Member member = memberRepository.save(Member.register(ExternalAccount.of("kakao", "1234"), "nick"));
            Badge badge1 = badgeRepository.save(Badge.of("뱃지1", "image1"));
            member.changeBadge(badge1.getId());
            MemberBadge memberBadge1 = badge1.issue(member.getId());
            badgeRepository.save(memberBadge1);
            Badge badge2 = badgeRepository.save(Badge.of("뱃지2", "image2"));
            MemberBadge memberBadge2 = badge2.issue(member.getId());
            badgeRepository.save(memberBadge2);

            ParameterizedTypeReference<ApiResponse<MemberResponse.Badge>> responseType = new ParameterizedTypeReference<>() {
            };
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("X-USER-ID", member.getId().toString());
            MemberRequest.Badge request = new MemberRequest.Badge(badge2.getId());

            ResponseEntity<ApiResponse<MemberResponse.Badge>> response = testRestTemplate.exchange(
                    "/api/members/me/badge",
                    HttpMethod.PATCH, new HttpEntity<>(request, httpHeaders), responseType);

            assertThat(response.getBody().getResult().id()).isEqualTo(member.getId());
            assertThat(response.getBody().getResult().badgeImageUrl()).isEqualTo(badge2.getImageUrl());
        }
    }

    @Nested
    @DisplayName("GET /api/members/{memberId}/badge")
    class GetMemberBadge {
        @Test
        @DisplayName("특정 유저의 뱃지를 조회한다.")
        void getMemberBadge() {
            Member member1 = memberRepository.save(Member.register(ExternalAccount.of("kakao", "1234"), "nick1"));
            Badge badge1 = badgeRepository.save(Badge.of("뱃지1", "image1"));
            member1.changeBadge(badge1.getId());
            MemberBadge memberBadge1 = badge1.issue(member1.getId());
            badgeRepository.save(memberBadge1);

            Member member2 = memberRepository.save(Member.register(ExternalAccount.of("kakao", "5678"), "nick2"));

            ParameterizedTypeReference<ApiResponse<MemberResponse.Badge>> responseType = new ParameterizedTypeReference<>() {
            };
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("X-USER-ID", member2.getId().toString());

            ResponseEntity<ApiResponse<MemberResponse.Badge>> response = testRestTemplate.exchange(
                    "/api/members/" + member1.getId() + "/badge",
                    HttpMethod.GET, new HttpEntity<>(httpHeaders), responseType);

            assertThat(response.getBody().getResult().id()).isEqualTo(member1.getId());
            assertThat(response.getBody().getResult().badgeImageUrl()).isEqualTo(badge1.getImageUrl());
        }
    }
}