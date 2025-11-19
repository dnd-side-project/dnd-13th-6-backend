package com.runky.auth.infrastructure.erternal.dto;

import java.util.List;

public record ApplePublicKeys(
	List<ApplePublicKey> keys
) {
}
