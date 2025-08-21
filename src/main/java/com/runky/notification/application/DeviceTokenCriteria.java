package com.runky.notification.application;

import com.runky.notification.domain.push.DeviceToken;

public sealed interface DeviceTokenCriteria {

	record Register(Long memberId,String token)implements DeviceTokenCriteria{

	}

	record Delete(Long memberId,String token)implements DeviceTokenCriteria{

	}
}
