package com.example.playlist.member.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSocial {
    private Long id;
    private Long memberId;
    private String provider;
    private String providerId;
    private LocalDateTime createdAt;

    public static MemberSocial of(Long memberId, Provider provider, String providerId) {
        return MemberSocial.builder()
                .memberId(memberId)
                .provider(provider.name())
                .providerId(providerId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}