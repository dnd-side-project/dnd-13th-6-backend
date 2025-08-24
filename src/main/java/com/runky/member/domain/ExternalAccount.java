package com.runky.member.domain;

import org.hibernate.annotations.Comment;

import com.runky.member.error.InvalidExternalAccountException;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalAccount {

	@Comment("카카오 등 외부 provider 이름 ex. kakao")
	@Column(name = "provider", nullable = false, length = 32)
	private String provider;

	@Comment("카카오 등 외부 provider 식별자")
	@Column(name = "provider_id", nullable = false, length = 64)
	private String providerId;

	private ExternalAccount(String provider, String providerId) {
		
		if (provider == null || provider.isBlank())
			throw new InvalidExternalAccountException();
		if (providerId == null || providerId.isBlank())
			throw new InvalidExternalAccountException();

		provider = provider.trim().toLowerCase();
		providerId = providerId.trim();

		if (providerId.length() > 64)
			throw new InvalidExternalAccountException();

		this.provider = provider;
		this.providerId = providerId;
	}

	public static ExternalAccount of(String provider, String providerId) {
		return new ExternalAccount(provider, providerId);
	}

	public String provider() {
		return provider;
	}

	public String providerId() {
		return providerId;
	}
}
