package com.example.playlist.member.exception;

import com.example.playlist.global.error.BaseException;

public class MemberException extends BaseException {
    public MemberException(MemberErrorCode message) {
        super(message);
    }
}
