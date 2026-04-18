package com.example.playlist.member.oauth2;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * OAuth2 로그인 후 SecurityContext에 담길 사용자 객체.
 * loginId, memberId, profileComplete 정보를 추가로 보유한다.
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String loginId;
    private final Long memberId;
    private final boolean profileComplete;

    public CustomOAuth2User(Map<String, Object> attributes,
                            String nameAttributeKey,
                            String loginId,
                            Long memberId,
                            boolean profileComplete) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.loginId = loginId;
        this.memberId = memberId;
        this.profileComplete = profileComplete;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return attributes.get(nameAttributeKey).toString();
    }
}