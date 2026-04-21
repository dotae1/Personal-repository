package com.example.playlist.member.oauth2;

import com.example.playlist.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 후처리:
 * - profileComplete=true  → AccessToken + RefreshToken 발급 후 프론트 메인 페이지로 리다이렉트
 * - profileComplete=false → TempToken 발급 후 프론트 추가정보 입력 페이지로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();

        if (!oauth2User.isProfileComplete()) {
            // 신규 소셜 회원 → 추가정보 입력 필요
            String tempToken = jwtUtil.createTempToken(oauth2User.getMemberId());
            jwtUtil.sendTempToken(response, tempToken);
            log.info("[OAuth2] 추가정보 입력 필요 - memberId={}", oauth2User.getMemberId());
            getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/profile/complete");
            return;
        }

        // 기존 회원 → 정상 토큰 발급
        String loginId = oauth2User.getLoginId();
        String role = oauth2User.getRole() != null ? oauth2User.getRole().name() : "USER";
        String accessToken = jwtUtil.createAccessToken(loginId, role);
        String refreshToken = jwtUtil.createRefreshToken(loginId);
        jwtUtil.saveRefreshToken(loginId, refreshToken);
        jwtUtil.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        log.info("[OAuth2] 로그인 성공 - loginId={}", loginId);
        getRedirectStrategy().sendRedirect(request, response, frontendUrl);
    }
}
