package com.example.playlist.member.oauth2;

import com.example.playlist.member.entity.Member;
import com.example.playlist.member.entity.MemberSocial;
import com.example.playlist.member.entity.Provider;
import com.example.playlist.member.repository.MemberMapper;
import com.example.playlist.member.repository.MemberSocialMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Google / Spotify OAuth2 콜백을 단일 서비스에서 처리.
 * - Google : scope에 openid 미포함 → 일반 OAuth2UserService로 진입
 * - Spotify: 커스텀 provider 등록
 *
 * 처리 순서:
 * 1. member_social (provider + providerId) 조회 → 기존 소셜 계정이면 바로 로그인
 * 2. member (email) 조회 → 다른 경로로 가입된 계정이면 소셜 계정 연결 (계정 통합)
 * 3. 둘 다 없으면 신규 member + member_social 생성, profileComplete=false
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberMapper memberMapper;
    private final MemberSocialMapper memberSocialMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public static final String SPOTIFY_USER_TOKEN_KEY = "SPOTIFY_USER_TOKEN:";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        OAuthAttributes oAuthAttributes = OAuthAttributes.of(registrationId, attributes);

        Member member = processOAuth2User(oAuthAttributes);

        // Spotify 로그인 시 유저 액세스 토큰을 Redis에 저장
        if ("spotify".equalsIgnoreCase(registrationId)) {
            String spotifyAccessToken = userRequest.getAccessToken().getTokenValue();
            Instant expiresAt = userRequest.getAccessToken().getExpiresAt();
            long ttlSeconds = expiresAt != null
                    ? Duration.between(Instant.now(), expiresAt).getSeconds()
                    : 3600L;
            redisTemplate.opsForValue().set(
                    SPOTIFY_USER_TOKEN_KEY + member.getId(),
                    spotifyAccessToken,
                    Duration.ofSeconds(Math.max(ttlSeconds, 60))
            );
            log.info("[OAuth2] Spotify 유저 토큰 저장 - memberId={}", member.getId());
        }

        return new CustomOAuth2User(
                attributes,
                oAuthAttributes.getNameAttributeKey(),
                member.getLoginId(),
                member.getId(),
                member.isProfileComplete(),
                member.getRole()
        );
    }

    private Member processOAuth2User(OAuthAttributes attrs) {
        String provider = attrs.getProvider().name();
        String providerId = attrs.getProviderId();
        String email = attrs.getEmail();
        String name = attrs.getName();

        // 1. member_social에 이미 등록된 소셜 계정인지 확인
        Optional<MemberSocial> existingSocial = memberSocialMapper.findByProviderAndProviderId(provider, providerId);
        if (existingSocial.isPresent()) {
            Member member = memberMapper.findById(existingSocial.get().getMemberId())
                    .orElseThrow(() -> new OAuth2AuthenticationException("회원 정보를 찾을 수 없습니다."));
            log.info("[OAuth2] 기존 소셜 계정 로그인 - provider={}, email={}", provider, email);
            return member;
        }

        // 2. 동일 이메일로 가입된 계정이 있으면 소셜 계정 연결 (계정 통합)
        Optional<Member> existingMember = memberMapper.findByEmail(email);
        if (existingMember.isPresent()) {
            Member member = existingMember.get();
            MemberSocial memberSocial = MemberSocial.of(member.getId(), attrs.getProvider(), providerId);
            memberSocialMapper.save(memberSocial);
            log.info("[OAuth2] 기존 회원에 소셜 계정 연결(통합) - provider={}, email={}", provider, email);
            return member;
        }

        // 3. 신규 회원 생성
        String loginId = attrs.getProvider().name().toLowerCase() + "_" + providerId;
        Member newMember = Member.createSocialMember(loginId, email, name, attrs.getProvider());
        memberMapper.save(newMember);

        // 저장 후 ID가 채워진 member 다시 조회
        Member savedMember = memberMapper.findByLoginId(loginId)
                .orElseThrow(() -> new OAuth2AuthenticationException("회원 저장에 실패했습니다."));

        MemberSocial memberSocial = MemberSocial.of(savedMember.getId(), attrs.getProvider(), providerId);
        memberSocialMapper.save(memberSocial);

        log.info("[OAuth2] 신규 소셜 회원 생성 - provider={}, email={}", provider, email);
        return savedMember;
    }
}
