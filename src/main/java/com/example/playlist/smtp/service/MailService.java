package com.example.playlist.smtp.service;

import com.example.playlist.smtp.dto.MailRequest;
import com.example.playlist.smtp.dto.MailVerificationRequest;
import com.example.playlist.smtp.exception.MailErrorCode;
import com.example.playlist.smtp.exception.MailSuccessCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.mail.username}")
    String sendEmail;

    private static final String MAIL_PREFIX = "mail:";
    private static final long TTL_MINUTES = 5;

    public MimeMessage createCodeEmail(String email, String code) throws MessagingException {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            mimeMessage.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(email));
            mimeMessage.setSubject("인증번호입니다.");
            mimeMessage.setText("이메일 인증코드 : " + code);
            mimeMessage.setFrom(sendEmail);
            return mimeMessage;
        } catch (MessagingException e) {
            throw new MailSendException("이메일 생성 중 오류 발생");
        }
    }

    public void sendMail(MimeMessage email) {
        try {
            mailSender.send(email);
        } catch (MailException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
    }

    private String createAuthNumber() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    public void sendCertificationMail(MailRequest mailRequest) throws MessagingException {
        String code = createAuthNumber();
        sendMail(createCodeEmail(mailRequest.getEmail(), code));
        redisTemplate.opsForValue().set(MAIL_PREFIX + mailRequest.getEmail(), code, TTL_MINUTES, TimeUnit.SECONDS);
    }

    //TODO : redis 활용하여, verify Key를 활용해 회원가입 할 때 이 이메일에 존재하는지 여부 체크
    public MailErrorCode verifyCode(MailVerificationRequest request) {
        String savedCode = redisTemplate.opsForValue().get(MAIL_PREFIX + request.getEmail());
        Long ttl = redisTemplate.getExpire(request.getEmail(), TimeUnit.SECONDS);

        if (savedCode == null || ttl == -2) {
            return MailErrorCode.CODE_INVALID;
        } if (!savedCode.equals(request.getCode())) {
            return MailErrorCode.CODE_IS_NOT_CORRECT;
        }

        redisTemplate.delete(MAIL_PREFIX + request.getEmail());
        return MailErrorCode.CODE_OK;
    }
}