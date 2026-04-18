package com.example.playlist.member.oauth2;

import com.example.playlist.member.entity.Provider;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Google / Spotify OAuth2 응답에서 공통 필드를 추출하는 VO.
 */
@Getter
@Builder
public class OAuthAttributes {

    private Provider provider;
    private String providerId;
    private String email;
    private String name;
    private String nameAttributeKey;

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> ofGoogle(attributes);
            case "spotify" -> ofSpotify(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        };
    }

    /**
     * Google OAuth2 (openid 스코프 미포함 → userinfo 엔드포인트 응답)
     * 주요 필드: sub, email, name, picture
     */
    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .provider(Provider.GOOGLE)
                .providerId(attributes.get("sub").toString())
                .email(attributes.get("email").toString())
                .name(attributes.getOrDefault("name", "").toString())
                .nameAttributeKey("sub")
                .build();
    }

    /**
     * Spotify OAuth2
     * 주요 필드: id, email, display_name
     */
    private static OAuthAttributes ofSpotify(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .provider(Provider.SPOTIFY)
                .providerId(attributes.get("id").toString())
                .email(attributes.getOrDefault("email", "").toString())
                .name(attributes.getOrDefault("display_name", "").toString())
                .nameAttributeKey("id")
                .build();
    }
}
