package com.example.playlist.member.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private Long id;
    private String loginId;
    private String email;
    private String nickname;
    private String password;
    private Gender gender;
    private int age;
    private Provider provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;

    public static Member createMember(String loginId, String email, String nickname, String password, Gender gender, int age) {
        return Member.builder()
                .loginId(loginId)
                .email(email)
                .nickname(nickname)
                .password(password)
                .gender(gender)
                .age(age)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}