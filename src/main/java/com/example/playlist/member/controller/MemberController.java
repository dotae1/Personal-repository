package com.example.playlist.member.controller;

import com.example.playlist.global.success.SuccessResponse;
import com.example.playlist.member.dto.JoinRequest;
import com.example.playlist.member.exception.MemberSuccessCode;
import com.example.playlist.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
