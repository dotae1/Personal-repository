package com.example.playlist.mail.exception;

import com.example.playlist.global.error.BaseException;

public class MailException extends BaseException {
    public MailException(MailErrorCode message) {
        super(message);
    }
}

