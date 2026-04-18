package com.example.playlist.member.service;

import com.example.playlist.global.util.JwtUtil;
import com.example.playlist.global.util.RedisUtil;
import com.example.playlist.member.dto.JoinRequest;
import com.example.playlist.member.dto.JoinResponse;
import com.example.playlist.member.dto.MemberInfoResponse;
import com.example.playlist.member.dto.SocialProfileCompleteRequest;
import com.example.playlist.member.entity.Member;
import com.example.playlist.member.exception.MemberErrorCode;
import com.example.playlist.member.exception.MemberException;
import com.example.playlist.member.repository.MemberMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final RedisUtil redisUtil;
    private final MemberMapper memberMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;

    public JoinResponse join(JoinRequest request) {
        String verified = redisUtil.getData("verified:" + request.getEmail());

        if (verified == null || !verified.equals(request.getEmail())) {
            throw new MemberException(MemberErrorCode.MAIL_VERIFIED_FAILED);
        }

        memberMapper.findByEmail(request.getEmail())
                .ifPresent(member -> {
                    throw new MemberException(MemberErrorCode.MEMBER_ALREADY_EXIST);
                });

        Member member = Member.createMember(
                request.getLoginId(),
                request.getEmail(),
                bCryptPasswordEncoder.encode(request.getPassword()),
                request.getNickname(),
                request.getName(),
                request.getGender(),
                request.getAge()
        );

        memberMapper.save(member);

        return JoinResponse.of(member.getLoginId(), member.getNickname());
    }

    public void login(String loginId, String password, HttpServletResponse response) {
        Member member = memberMapper.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (!bCryptPasswordEncoder.matches(password, member.getPassword())) {
            throw new MemberException(MemberErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtUtil.createAccessToken(loginId);
        String refreshToken = jwtUtil.createRefreshToken(loginId);

        jwtUtil.saveRefreshToken(loginId, refreshToken);
        jwtUtil.sendAccessAndRefreshToken(response, accessToken, refreshToken);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response, String loginId) {
        jwtUtil.logout(request, response, loginId);
    }

    public MemberInfoResponse getMe(String loginId) {
        Member member = memberMapper.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        return MemberInfoResponse.from(member);
    }

    /**
     * 소셜 신규 회원 추가정보 입력 완료.
     * tempToken 쿠키를 검증 후 nickname/age/gender 저장 → 정식 토큰 발급.
     */
    public void completeProfile(HttpServletRequest request,
                                HttpServletResponse response,
                                SocialProfileCompleteRequest profileRequest) {
        Long memberId = jwtUtil.extractMemberIdFromTempToken(request)
                .orElseThrow(() -> new MemberException(MemberErrorCode.INVALID_TEMP_TOKEN));

        memberMapper.updateProfile(memberId, profileRequest.getNickname(), profileRequest.getAge(), profileRequest.getGender());

        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        String accessToken = jwtUtil.createAccessToken(member.getLoginId());
        String refreshToken = jwtUtil.createRefreshToken(member.getLoginId());
        jwtUtil.saveRefreshToken(member.getLoginId(), refreshToken);
        jwtUtil.sendAccessAndRefreshToken(response, accessToken, refreshToken);
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) {
        Member member = memberMapper.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        return User.builder()
                .username(member.getLoginId())
                .password(member.getPassword())
                .build();
    }
}