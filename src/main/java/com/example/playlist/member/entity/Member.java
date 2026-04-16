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
    private int age;
    private Provider provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;

    public static Member createMember(String loginId, String email,String password, String nickname, String name, Gender gender, int age) {
        return Member.builder()
                .loginId(loginId)
                .email(email)
                .password(password)
                .nickname(nickname)
                .name(name)
                .gender(gender)
                .age(age)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}