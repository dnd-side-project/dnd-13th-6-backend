package com.runky.auth.application;

import com.runky.crew.domain.CrewCommand;
import com.runky.crew.domain.CrewService;
import com.runky.goal.domain.GoalCommand;
import com.runky.goal.domain.GoalService;
import com.runky.reward.domain.RewardCommand;
import com.runky.reward.domain.RewardService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.runky.auth.application.port.OAuthClient;
import com.runky.auth.domain.AuthInfo;
import com.runky.auth.domain.AuthTokenService;
import com.runky.auth.domain.port.TokenDecoder;
import com.runky.auth.domain.signup.SignupTokenService;
import com.runky.auth.domain.vo.OAuthUserInfo;
import com.runky.auth.domain.vo.RefreshTokenClaims;
import com.runky.member.domain.MemberCommand;
import com.runky.member.domain.dto.MemberInfo;
import com.runky.member.domain.service.MemberReader;
import com.runky.member.domain.service.MemberRegistrar;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthFacade {

	private final OAuthClient oAuthClient;
	private final SignupTokenService signupTokenService;

	private final MemberReader memberReader;
	private final MemberRegistrar memberRegistrar;

	private final TokenDecoder tokenDecoder;
	private final AuthTokenService authTokenService;

    private final RewardService rewardService;
    private final GoalService goalService;
    private final CrewService crewService;

    /**
	 * 1) code → accessToken → providerId 조회
	 * 2) 기존 회원이면 즉시 토큰 발급
	 * 3) 신규면 signupToken 발급
	 * 차후 exist + get -> Optional find로 리팩토링 필요
	 */
	@Transactional
	public AuthResult.OAuthLogin handleOAuthLogin(String authorizationCode) {
		String accessToken = oAuthClient.fetchAccessToken(authorizationCode);
		OAuthUserInfo info = oAuthClient.fetchUserInfo(accessToken);

		boolean exists = memberReader.existsByExternalAccount(info.provider(), info.providerId());

		if (!exists) {
			String signupToken = signupTokenService.issue(info);
			return AuthResult.OAuthLogin.newUser(signupToken);
		}

		MemberInfo.Summary member = memberReader.getInfoByExternalAccount(info.provider(), info.providerId());
		AuthInfo.TokenPair pair = authTokenService.issue(member.id(), member.role().name());
		return AuthResult.OAuthLogin.existing(pair.accessToken(), pair.refreshToken());
	}

	/**
	 * 프론트로부터 signupToken + 추가정보(닉네임)를 받아 최종 가입 후 토큰 발급
	 * 닉네임은 반드시 클라이언트 입력을 사용한다(카카오 닉네임 사용X)
	 */
	@Transactional
	public AuthResult.SigninComplete completeSignup(String signupToken, AuthCriteria.AdditionalSignUpData command) {
		OAuthUserInfo info = signupTokenService.get(signupToken);

		MemberInfo.Summary saved = memberRegistrar.registerFromExternal(
			new MemberCommand.RegisterFromExternal(
				info.provider(), info.providerId(), command.nickname()
			));
		signupTokenService.delete(signupToken);

        rewardService.init(new RewardCommand.Init(saved.id()));
        goalService.init(new GoalCommand.Init(saved.id()));
        crewService.init(new CrewCommand.Init(saved.id()));

		AuthInfo.TokenPair pair = authTokenService.issue(saved.id(), saved.role().name());
		return new AuthResult.SigninComplete(pair.accessToken(), pair.refreshToken());
	}

	/** RT로 AT/RT 재발급(회전) */
	@Transactional
	public AuthResult.rotatedToken reissueByRefresh(String refreshToken) {
		AuthInfo.TokenPair tokenPair = authTokenService.rotateByRefreshToken(refreshToken);
		return new AuthResult.rotatedToken(tokenPair.accessToken(), tokenPair.refreshToken());
	}

	@Transactional
	public void logoutByRefresh(String refreshToken) {
		RefreshTokenClaims decoded = tokenDecoder.decodeRefresh(refreshToken);
		authTokenService.delete(decoded.memberId());
	}
}
