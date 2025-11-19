package com.runky.auth.infrastructure.erternal.dto;

public record ApplePublicKey(
	String kty,  // Key Type: 키 타입 (예: "RSA")
	String kid,  // Key ID: 공개 키 식별자 (JWT 헤더의 kid와 매칭)
	String use,  // Public Key Use: 키 사용 용도 (예: "sig" - 서명용)
	String alg,  // Algorithm: 서명 알고리즘 (예: "RS256")
	String n,    // Modulus: RSA 공개 키의 modulus (Base64 URL 인코딩)
	String e     // Exponent: RSA 공개 키의 exponent (Base64 URL 인코딩)
) {
}
