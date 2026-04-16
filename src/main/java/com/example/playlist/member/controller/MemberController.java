package com.example.playlist.member.controller;

import com.example.playlist.global.success.SuccessResponse;
import com.example.playlist.member.dto.JoinRequest;
import com.example.playlist.member.dto.LoginRequest;
import com.example.playlist.member.exception.MemberSuccessCode;
import com.example.playlist.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<SuccessResponse> join(
            @RequestBody @Valid JoinRequest request
    ) {
        memberService.join(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(MemberSuccessCode.MEMBER_SUCCESS_SIGNUP));
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response
    ) {
        memberService.login(request.getLoginId(), request.getPassword(), response);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(MemberSuccessCode.LOGIN_SUCCESS));
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout(
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        memberService.logout(request, userDetails.getUsername());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(MemberSuccessCode.LOGOUT_SUCCESS));
    }
}