package com.example.playlist.member.service;

import com.example.playlist.global.RedisUtil;
import com.example.playlist.member.dto.JoinRequest;
import com.example.playlist.member.dto.JoinResponse;
import com.example.playlist.member.entity.Member;
import com.example.playlist.member.exception.MemberErrorCode;
import com.example.playlist.member.exception.MemberException;
import com.example.playlist.member.repository.MemberMapper;
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

    public JoinResponse join(JoinRequest request) {

        String verified = redisUtil.getData("verified" + request.getEmail());

        if(verified == null || !verified.equals(request.getEmail())) {
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
                request.getName(),
                request.getGender(),
                request.getAge()
        );

        memberMapper.save(member);

        return JoinResponse.of(member.getLoginId(), member.getNickname());
    }


    @Override
    public UserDetails loadUserByUsername(String loginId) throws MemberException {
        Member member = memberMapper.findByloginId(loginId);

        if(member == null) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        return User.builder()
                .username(member.getLoginId())
                .password(member.getPassword())
                .build();
    }
}
