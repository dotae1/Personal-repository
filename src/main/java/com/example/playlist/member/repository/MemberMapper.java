package com.example.playlist.member.repository;

import com.example.playlist.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface MemberMapper {

    Member findById(Long memberId);
    Optional<Member> findByEmail(String email);
    void save(Member member);
    Member findByLoginId(String loginId);
}
