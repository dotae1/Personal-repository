package com.example.playlist.mail.controller;

import com.example.playlist.global.error.ErrorResponse;
import com.example.playlist.global.success.SuccessResponse;
import com.example.playlist.mail.dto.MailRequest;
import com.example.playlist.mail.dto.MailVerificationRequest;
import com.example.playlist.mail.exception.MailErrorCode;
import com.example.playlist.mail.exception.MailSuccessCode;
import com.example.playlist.mail.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @Operation(
            summary = "메일 전송",
            description = "인증 메일을 전송하는 컨트롤러"
    )
    @PostMapping("/send")
    public ResponseEntity<SuccessResponse> sendMail(@RequestBody @Valid MailRequest request) throws MessagingException {
        mailService.sendCertificationMail(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(MailSuccessCode.MAIL_SUCCESS_SEND));
    }

    @Operation(
            summary = "메일 검증",
            description = "이메일 인증을 위해 보낸 코드를 검증하는 컨트롤러"
    )
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody @Valid MailVerificationRequest request) {
        MailErrorCode mailErrorCode = mailService.verifyCode(request);

        if(mailErrorCode == MailErrorCode.CODE_INVALID) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(mailErrorCode));
        } if (mailErrorCode == MailErrorCode.CODE_IS_NOT_CORRECT) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(mailErrorCode));
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(MailSuccessCode.MAIL_VERIFIED_SUCCESS));
    }
}