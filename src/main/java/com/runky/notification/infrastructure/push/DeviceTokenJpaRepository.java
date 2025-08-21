package com.runky.notification.infrastructure.push;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.runky.notification.domain.push.DeviceToken;

interface DeviceTokenJpaRepository extends JpaRepository<DeviceToken, Long> {

	@Query("""
		  select dt.token
		  from DeviceToken dt
		  where dt.memberId in :memberIds and dt.active = true
		""")
	List<String> findActiveTokensByMemberIds(@Param("memberIds") List<Long> memberIds);

	@Modifying
	@Query("delete from DeviceToken dt where dt.memberId = :memberId and dt.token = :token")
	int deleteByMemberIdAndToken(@Param("memberId") Long memberId, @Param("token") String token);

	@Modifying
	@Query("update DeviceToken dt set dt.active = false where dt.token in :tokens")
	void deactivateTokens(@Param("tokens") List<String> tokens);

	Optional<DeviceToken> findByMemberIdAndDeviceType(Long memberId, String deviceType);

	@Query("""
		  select case when count(dt) > 0 then true else false end
		  from DeviceToken dt
		  where dt.memberId = :memberId and dt.deviceType = :deviceType and dt.active = true
		""")
	boolean existsActiveByMemberIdAndDeviceType(@Param("memberId") Long memberId,
		@Param("deviceType") String deviceType);

}
