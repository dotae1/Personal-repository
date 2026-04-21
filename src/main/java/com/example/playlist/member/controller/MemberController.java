package com.example.playlist.member.controller;

import com.example.playlist.global.success.SuccessResponse;
import com.example.playlist.member.dto.JoinRequest;
import com.example.playlist.member.dto.LoginRequest;
import com.example.playlist.member.dto.MemberInfoResponse;
import com.example.playlist.member.dto.MyPageResponse;
import com.example.playlist.member.dto.SocialProfileCompleteRequest;
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
import org.springframework.web.bind.annotation.*;


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

    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<MemberInfoResponse>> getMe(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MemberInfoResponse info = memberService.getMe(userDetails.getUsername());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(MemberSuccessCode.LOGIN_SUCCESS, info));
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        memberService.logout(request, response, userDetails.getUsername());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(MemberSuccessCode.LOGOUT_SUCCESS));
    }

    @GetMapping("/mypage")
    public ResponseEntity<SuccessResponse<MyPageResponse>> getMyPage(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MyPageResponse myPage = memberService.getMyPage(userDetails.getUsername());
        return ResponseEntity.ok(SuccessResponse.of(MemberSuccessCode.MYPAGE_SUCCESS, myPage));
    }

    /**
     * 소셜 신규 회원 추가정보 입력 완료.
     * tempToken 쿠키가 필요하며, 성공 시 정식 AccessToken/RefreshToken이 발급된다.
     */
    @PostMapping("/profile/complete")
    public ResponseEntity<SuccessResponse> completeProfile(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody @Valid SocialProfileCompleteRequest profileRequest
    ) {
        memberService.completeProfile(request, response, profileRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.of(MemberSuccessCode.PROFILE_COMPLETE_SUCCESS));
    }
}