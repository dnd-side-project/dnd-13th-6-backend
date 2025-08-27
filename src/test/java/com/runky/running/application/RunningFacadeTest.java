package com.runky.running.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.crew.domain.Crew;
import com.runky.crew.domain.CrewCommand;
import com.runky.crew.domain.CrewMemberCount;
import com.runky.crew.domain.CrewRepository;
import com.runky.crew.domain.CrewService;
import com.runky.member.domain.ExternalAccount;
import com.runky.member.domain.Member;
import com.runky.member.domain.MemberRepository;
import com.runky.notification.domain.notification.NotificationCommand;
import com.runky.notification.domain.notification.NotificationInfo;
import com.runky.notification.domain.notification.NotificationService;
import com.runky.notification.domain.notification.NotificationTemplate;
import com.runky.utils.DatabaseCleanUp;

@SpringBootTest
class RunningFacadeTest {

	@Autowired
	CrewService crewService;
	@Autowired
	MemberRepository memberRepository;

	@Autowired
	RunningFacade runningFacade;
	@Autowired
	CrewRepository crewRepository;

	@Autowired
	NotificationService notificationService;

	@Autowired
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	@DisplayName("런닝을 시작하면 내 크루들의 모든 멤버들에게 알림이 전송된다.")
	@Test
	void when_run_start_then_push_all_CrewMembers() {
		// 멤버 1의 크루에는 1,2,3가 들어있고, 멤버 2의 크루에는 1,2만 들어있다.
		// given
		Member m1 = memberRepository.save(Member.register(ExternalAccount.of("provider1", "providerId1"), "닉넴1"));
		Member m2 = memberRepository.save(Member.register(ExternalAccount.of("provider2", "providerId2"), "닉넴2"));
		Member m3 = memberRepository.save(Member.register(ExternalAccount.of("provider3", "providerId3"), "닉넴3"));

		crewRepository.save(CrewMemberCount.of(m1.getId()));
		crewRepository.save(CrewMemberCount.of(m2.getId()));
		crewRepository.save(CrewMemberCount.of(m3.getId()));

		Crew crew1 = crewService.create(new CrewCommand.Create(m1.getId(), "멤버1의 크루"));
		Crew crew2 = crewService.create(new CrewCommand.Create(m2.getId(), "멤버2의 크루"));

		// crew1: 1,2,3
		crewService.join(new CrewCommand.Join(m2.getId(), crew1.getCode().value()));
		crewService.join(new CrewCommand.Join(m3.getId(), crew1.getCode().value()));

		// crew2: 1,2
		crewService.join(new CrewCommand.Join(m1.getId(), crew2.getCode().value()));
		// m2는 이미 리더라 포함됨

		// when
		RunningResult.Start result = runningFacade.start(new RunningCriteria.Start(m1.getId()));

		// then
		// 실행 결과 기본 검증
		assertThat(result.runningId()).isNotNull();
		assertThat(result.runningId()).isPositive();
		assertThat(result.startedAt()).isNotNull();
		assertThat(result.status()).isNotNull();

		// m2에게 최근 알림 1건
		NotificationInfo.Summaries s2 = notificationService.getRecentTopN(
			new NotificationCommand.GetRecentTopN(m2.getId(), 10));
		System.out.println(s2.values().get(0).message());
		assertThat(s2.values()).hasSize(1);
		assertThat(s2.values().get(0).template()).isEqualTo(NotificationTemplate.RUN_STARTED);
		assertThat(s2.values().get(0).message()).contains("닉넴1"); // 러너 닉네임 포함

		// m3에게 최근 알림 1건
		NotificationInfo.Summaries s3 = notificationService.getRecentTopN(
			new NotificationCommand.GetRecentTopN(m3.getId(), 10));
		assertThat(s3.values()).hasSize(1);
		assertThat(s3.values().get(0).template()).isEqualTo(NotificationTemplate.RUN_STARTED);
		assertThat(s3.values().get(0).message()).contains("닉넴1"); // 러너 닉네임 포함

	}

	@DisplayName("수신자가 0명(자기 혼자만 있는 크루)이면 푸시를 시도하지 않고 정상 응답한다")
	@Test
	void start_when_no_receivers_should_skip_push_and_return_ok() {
		// given: m1만 포함된 크루(수신자 0명)
		Member m1 = memberRepository.save(Member.register(ExternalAccount.of("provider1", "pid1"), "접시1"));
		crewRepository.save(CrewMemberCount.of(m1.getId()));

		Crew crew1 = crewService.create(new CrewCommand.Create(m1.getId(), "6조의 크루"));
		assertThat(crew1).isNotNull();

		// when
		RunningResult.Start result = runningFacade.start(new RunningCriteria.Start(m1.getId()));

		// then: 런 시작 결과는 정상
		assertThat(result.runningId()).isPositive();
		assertThat(result.startedAt()).isNotNull();
		assertThat(result.status()).isNotNull();

		// 그리고 어떤 멤버에게도 알림이 저장되지 않는다 - (수신자 자체가 없으므로)
		NotificationInfo.Summaries s1 = notificationService.getRecentTopN(
			new NotificationCommand.GetRecentTopN(m1.getId(), 10));
		assertThat(s1.values()).isEmpty();
	}

}
