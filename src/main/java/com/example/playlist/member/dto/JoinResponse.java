package com.example.playlist.member.dto;

import lombok.Getter;

@Getter
public class JoinResponse {
    private String loginId;
    private String nickname;

    private JoinResponse(String loginId, String nickname) {
        this.loginId = loginId;
        this.nickname = nickname;
    }

    public static JoinResponse of(String loginId, String nickname) {
        return new JoinResponse(loginId, nickname);
    }
}
