package com.runky.notification.domain.push;

import java.util.List;

public sealed interface PushInfo {

	sealed interface SenTPush extends PushInfo {
		record Summary(int success, int failure, List<String> invalidTokens) implements SenTPush {
		}

	}

	sealed interface DeviceTokenInfo extends PushInfo {

		record DeletionResult(int count) implements DeviceTokenInfo {
		}

		record ActiveToken(String token) implements DeviceTokenInfo {
		}

		record ActiveTokens(List<String> tokens) implements DeviceTokenInfo {
		}

		record ExistenceCheck(boolean exists) implements DeviceTokenInfo {
		}
	}

}
