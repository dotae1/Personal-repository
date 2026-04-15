package com.example.playlist.smtp.service;

import com.example.playlist.global.RedisUtil;
import com.example.playlist.smtp.dto.MailRequest;
import com.example.playlist.smtp.dto.MailVerificationRequest;
import com.example.playlist.smtp.exception.MailErrorCode;
import com.example.playlist.smtp.exception.MailSuccessCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final RedisUtil redisUtil;

    @Value("${spring.mail.username}")
    String sendEmail;


    public MimeMessage createCodeEmail(String email, String code) throws MessagingException {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            mimeMessage.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(email));
            mimeMessage.setSubject("인증번호입니다.");
            mimeMessage.setText("이메일 인증코드 : " + code);
            mimeMessage.setFrom(sendEmail);
            return mimeMessage;
        } catch (MessagingException e) {
            throw new MailSendException("이메일 생성 중 오류 발생하였습니다. 다시 시도해주세요!");
        }
    }

    public void sendMail(MimeMessage email) {
        try { mailSender.send(email);}
        catch (MailException e) {log.error("메일 전송 실패", e);}
    }

    private String createAuthNumber() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    public String sendCertificationMail(MailRequest mailRequest) throws MessagingException {
        String code = createAuthNumber();

        sendMail(createCodeEmail(mailRequest.getEmail(), code));
        redisUtil.setDataExpire(mailRequest.getEmail(), code, 180L);

        return code;
    }

    //TODO : redis 활용하여, verify Key를 활용해 회원가입 할 때 이 이메일에 존재하는지 여부 체크
    public MailErrorCode verifyCode(MailVerificationRequest request) {
        String savedCode = redisUtil.getData(request.getEmail());
        Long ttl = redisUtil.getExpire(request.getEmail(), TimeUnit.SECONDS);

        if (savedCode == null || ttl == -2) {
            return MailErrorCode.CODE_INVALID;
        } if (!savedCode.equals(request.getCode())) {
            return MailErrorCode.CODE_IS_NOT_CORRECT;
        }

        redisUtil.setDataExpire("verified: " + request.getEmail(), request.getEmail(), 600);
        redisUtil.deleteData(request.getEmail());

        return MailErrorCode.CODE_OK;
    }
}