package com.runky.notification.api;

import org.aspectj.weaver.patterns.IToken;

import com.runky.notification.application.DeviceTokenCriteria;
import com.runky.notification.domain.push.DeviceToken;

public sealed interface DeviceTokenRequest {

	record Register(String token) implements DeviceTokenRequest{
		DeviceTokenCriteria.Register toCriteria(Long memberId){
			return new DeviceTokenCriteria.Register(memberId,token);
		}
	}

	record Delete(String token) implements DeviceTokenRequest{
		DeviceTokenCriteria.Delete toCriteria(Long memberId){
			return new DeviceTokenCriteria.Delete(memberId,token);

		}
	}

}
