package com.runky.running.interfaces.api;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.runky.global.response.ApiResponse;
import com.runky.running.domain.Running;
import com.runky.running.domain.RunningRepository;
import com.runky.running.domain.RunningTrack;
import com.runky.running.domain.RunningTrackRepository;
import com.runky.utils.DatabaseCleanUp;
import com.runky.utils.TestTokenIssuer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RunningApiE2ETest {

	private static final String END_URL = "/api/runnings/{runningId}/end";
	@Autowired
	private RunningRepository runningRepository;
	@Autowired
	private RunningTrackRepository trackRepository;
	@Autowired
	private TestRestTemplate rest;
	@Autowired
	private TestTokenIssuer tokenIssuer;
	@Autowired
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	private RunningRequest.End buildEndRequest() {
		var summary = new RunningRequest.End.Summary(
			1200.0,
			600L,
			2.0
		);
		var track = new RunningRequest.End.Track(
			"GEOJSON",
			"""
				{"type":"LineString","coordinates":[[127.0,37.0],[127.001,37.001]]}
				""".trim(),
			2
		);
		return new RunningRequest.End(summary, track);
	}

	@Nested
	@DisplayName("POST /api/runnings/start")
	class StartRunning {

		private final String URL = "/api/runnings/start";

		@Test
		@DisplayName("런닝을 시작하면 publishDestination과 런 식별자들이 반환된다.")
		void start() {
			// given
			long memberId = 1L;
			HttpHeaders headers = tokenIssuer.issue(memberId, "USER");

			ParameterizedTypeReference<ApiResponse<RunningResponse.Start>> type =
				new ParameterizedTypeReference<>() {
				};

			// when
			ResponseEntity<ApiResponse<RunningResponse.Start>> res =
				rest.exchange(URL, HttpMethod.POST, new HttpEntity<>(headers), type);

			// then
			assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
			RunningResponse.Start body = res.getBody().getResult();
			assertThat(body).isNotNull();
			assertThat(body.runningId()).isPositive();
			assertThat(body.runnerId()).isEqualTo(memberId);
			assertThat(body.status()).isNotBlank();
			assertThat(body.startedAt()).isNotNull();
			assertThat(body.pub())
				.isEqualTo("/app/runnings/" + body.runningId() + "/location");
		}
	}

	@Nested
	@DisplayName("POST /api/runnings/{runningId}/end")
	class EndRunning {

		private final String START_URL = "/api/runnings/start";
		private final String END_URL = "/api/runnings/{runningId}/end";

		@Test
		@DisplayName("시작한 런닝을 종료하면 종료 정보와 트랙 저장이 정상 동작한다.")
		void end() {
			// given: start
			long memberId = 10L;
			HttpHeaders headers = tokenIssuer.issue(memberId, "USER");

			ParameterizedTypeReference<ApiResponse<RunningResponse.Start>> startType =
				new ParameterizedTypeReference<>() {
				};

			ResponseEntity<ApiResponse<RunningResponse.Start>> startRes =
				rest.exchange(START_URL, HttpMethod.POST, new HttpEntity<>(headers), startType);

			RunningResponse.Start started = startRes.getBody().getResult();
			Long runningId = started.runningId();

			// end 요청 payload 구성
			var summary = new RunningRequest.End.Summary(
				1200.0,
				600L,
				2.0
			);
			var track = new RunningRequest.End.Track(
				"GEOJSON",
				"""
					{"type":"LineString","coordinates":[[127.0,37.0],[127.0005,37.0005]]}
					""".trim(),
				2
			);
			var endReq = new RunningRequest.End(summary, track);

			ParameterizedTypeReference<ApiResponse<RunningResponse.End>> endType =
				new ParameterizedTypeReference<>() {
				};

			// when: end
			ResponseEntity<ApiResponse<RunningResponse.End>> endRes =
				rest.exchange(END_URL, HttpMethod.POST, new HttpEntity<>(endReq, headers), endType, runningId);

			// then
			assertThat(endRes.getStatusCode()).isEqualTo(HttpStatus.OK);
			RunningResponse.End ended = endRes.getBody().getResult();
			assertThat(ended).isNotNull();
			assertThat(ended.runningId()).isEqualTo(runningId);
			assertThat(ended.runnerId()).isEqualTo(memberId);
			assertThat(ended.startedAt()).isNotNull();
			assertThat(ended.endedAt()).isNotNull();
			assertThat(ended.endedAt()).isAfterOrEqualTo(ended.startedAt());
		}
	}

	@Nested
	@DisplayName("GET /api/runnings/today")
	class TodayPushSummary {

		private final String START_URL = "/api/runnings/start";
		private final String END_URL = "/api/runnings/{runningId}/end";
		private final String TODAY_URL = "/api/runnings/today";

		@Test
		@DisplayName("오늘 완료한 런닝들의 합산 요약을 조회한다.")
		void getToday() {
			// given: 오늘 한 번 달리고 종료
			long memberId = 100L;
			HttpHeaders headers = tokenIssuer.issue(memberId, "USER");

			ParameterizedTypeReference<ApiResponse<RunningResponse.Start>> startType =
				new ParameterizedTypeReference<>() {
				};
			ParameterizedTypeReference<ApiResponse<RunningResponse.End>> endType =
				new ParameterizedTypeReference<>() {
				};
			ParameterizedTypeReference<ApiResponse<RunningResponse.TodaySummary>> todayType =
				new ParameterizedTypeReference<>() {
				};

			// start
			var startRes = rest.exchange(START_URL, HttpMethod.POST, new HttpEntity<>(headers), startType);
			Long runningId = startRes.getBody().getResult().runningId();

			// end
			var endReq = new RunningRequest.End(
				new RunningRequest.End.Summary(1500.0, 900L, 1.6667),
				new RunningRequest.End.Track("GEOJSON",
					"""
						{"type":"LineString","coordinates":[[127.0,37.0],[127.001,37.001],[127.002,37.002]]}
						""".trim(),
					3
				)
			);
			rest.exchange(END_URL, HttpMethod.POST, new HttpEntity<>(endReq, headers), endType, runningId);

			// when: 오늘 요약
			ResponseEntity<ApiResponse<RunningResponse.TodaySummary>> todayRes =
				rest.exchange(TODAY_URL, HttpMethod.GET, new HttpEntity<>(headers), todayType);

			// then
			assertThat(todayRes.getStatusCode()).isEqualTo(HttpStatus.OK);
			RunningResponse.TodaySummary summary = todayRes.getBody().getResult();
			assertThat(summary).isNotNull();
			assertThat(summary.totalDistanceMeter()).isNotNull().isGreaterThan(0.0);
			assertThat(summary.durationSeconds()).isNotNull().isGreaterThan(0L);
			assertThat(summary.avgSpeedMps()).isNotNull().isGreaterThan(0.0);
		}
	}

	@Nested
	@DisplayName("POST /api/runnings/{id}/end - 실패 케이스")
	class EndFailures {

		@Test
		@DisplayName("이미 종료된 런닝을 다시 종료하면 ALREADY_ENDED_RUNNING")
		void end_again_should_fail_with_ALREADY_ENDED_RUNNING() {
			// given
			long runnerId = 202L;
			HttpHeaders headers = tokenIssuer.issue(runnerId, "USER");

			Running running = runningRepository.save(Running.start(runnerId, LocalDateTime.now()));
			Long runningId = running.getId();

			// 1차 종료(정상)
			ParameterizedTypeReference<ApiResponse<RunningResponse.End>> okType = new ParameterizedTypeReference<>() {
			};
			ResponseEntity<ApiResponse<RunningResponse.End>> ok =
				rest.exchange(END_URL, HttpMethod.POST, new HttpEntity<>(buildEndRequest(), headers), okType,
					runningId);
			assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);

			// when: 2차 종료 요청
			ResponseEntity<String> err =
				rest.exchange(END_URL, HttpMethod.POST, new HttpEntity<>(buildEndRequest(), headers), String.class,
					runningId);

			// then
			assertThat(err.getStatusCode().is4xxClientError()).isTrue();
			assertThat(err.getBody()).contains("이미 종료된 런닝입니다.");
		}

		@Test
		@DisplayName("다른 유저가 남의 런닝을 종료하려 하면 NOT_FOUND_RUNNING")
		void end_by_other_user_should_fail_with_NOT_FOUND_RUNNING() {
			// given
			long ownerId = 303L;
			long attackerId = 304L;

			Running running = runningRepository.save(Running.start(ownerId, LocalDateTime.now()));
			Long runningId = running.getId();

			HttpHeaders attackerHeaders = tokenIssuer.issue(attackerId, "USER");

			// when
			ResponseEntity<String> res =
				rest.exchange(END_URL, HttpMethod.POST, new HttpEntity<>(buildEndRequest(), attackerHeaders),
					String.class, runningId);

			// then
			assertThat(res.getBody()).contains("런닝을 찾을 수 없습니다.");
		}

		@Test
		@DisplayName("이미 트랙이 존재하는 활성 런닝을 종료하면 TRACK_ALREADY_EXISTS")
		void end_with_existing_track_should_fail_with_TRACK_ALREADY_EXISTS() {
			// given: 아직 종료하지 않았지만, 트랙이 이미 저장된 비정상 상태를 구성
			long runnerId = 405L;
			HttpHeaders headers = tokenIssuer.issue(runnerId, "USER");

			Running running = runningRepository.save(Running.start(runnerId, LocalDateTime.now()));
			Long runningId = running.getId();

			// 사전 삽입: 트랙 중복 상태
			trackRepository.save(new RunningTrack(
				running,
				"GEOJSON",
				"{\"type\":\"LineString\",\"coordinates\":[[127.0,37.0]]}",
				1
			));

			// when
			ResponseEntity<String> res =
				rest.exchange(END_URL, HttpMethod.POST, new HttpEntity<>(buildEndRequest(), headers), String.class,
					runningId);

			// then
			assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
			assertThat(res.getBody()).contains("이미 트랙이 저장되어 있습니다.");
		}
	}
}
