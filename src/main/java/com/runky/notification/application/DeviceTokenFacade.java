package com.runky.notification.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.runky.notification.domain.push.DeviceTokenCommand;
import com.runky.notification.domain.push.DeviceTokenInfo;
import com.runky.notification.domain.push.DeviceTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceTokenFacade {

	private final DeviceTokenService deviceTokenService;

	@Transactional
	public void register(DeviceTokenCriteria.Register criteria) {
		deviceTokenService.register(new DeviceTokenCommand.Register(criteria.memberId(), criteria.token()));
	}

	@Transactional
	public DeviceTokenResult.Delete delete(DeviceTokenCriteria.Delete criteria) {
		DeviceTokenInfo.Delete info = deviceTokenService.delete(
			new DeviceTokenCommand.Delete(criteria.memberId(), criteria.token()));

		return new DeviceTokenResult.Delete(info.count());
	}
}
