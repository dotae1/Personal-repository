package com.example.playlist.member.dto;

import com.example.playlist.member.entity.Gender;
import com.example.playlist.member.entity.Member;
import com.example.playlist.member.entity.Provider;
import com.example.playlist.member.entity.Role;

public record MemberInfoResponse(
        Long id,
        String loginId,
        String email,
        String name,
        String nickname,
        Gender gender,
        Integer age,
        Provider provider,
        Role role,
        boolean profileComplete
) {
    public static MemberInfoResponse from(Member member) {
        return new MemberInfoResponse(
                member.getId(),
                member.getLoginId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getGender(),
                member.getAge(),
                member.getProvider(),
                member.getRole(),
                member.isProfileComplete()
        );
    }
}