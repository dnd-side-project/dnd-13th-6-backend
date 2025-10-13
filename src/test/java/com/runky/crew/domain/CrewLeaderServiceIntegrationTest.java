package com.runky.crew.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.runky.utils.DatabaseCleanUp;

@SpringBootTest
class CrewLeaderServiceIntegrationTest {
	@Autowired
	private CrewLeaderService crewLeaderService;
	@Autowired
	private CrewRepository crewRepository;
	@Autowired
	private DatabaseCleanUp databaseCleanUp;

	@AfterEach
	void tearDown() {
		databaseCleanUp.truncateAllTables();
	}

	@Nested
	@DisplayName("크루 해체 시,")
	class Disband {
		@Test
		@DisplayName("크루는 삭제된다.")
		void deleteCrew() {
			Crew crew = Crew.of(new CrewCommand.Create(1L, "name"), new Code("ABC123"));
			Crew save = crewRepository.save(crew);

			crewLeaderService.disband(new CrewCommand.Disband(1L, 1L));

			Optional<Crew> find = crewRepository.findById(save.getId());
			assertThat(find).isEmpty();
		}
	}
}