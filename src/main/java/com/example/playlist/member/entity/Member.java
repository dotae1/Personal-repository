package com.example.playlist.member.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private Long id;
    private String loginId;
    private String email;
    private String name;
    private String nickname;
    private String password;
    private Gender gender;
    private Integer age;
    private Provider provider;
    private Role role;
    private boolean profileComplete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;

    public static Member createMember(String loginId, String email, String password, String nickname, String name, Gender gender, int age) {
        return Member.builder()
                .loginId(loginId)
                .email(email)
                .password(password)
                .nickname(nickname)
                .name(name)
                .gender(gender)
                .age(age)
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .profileComplete(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Member createSocialMember(String loginId, String email, String name, Provider provider) {
        return Member.builder()
                .loginId(loginId)
                .email(email)
                .name(name)
                .provider(provider)
                .role(Role.USER)
                .profileComplete(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}