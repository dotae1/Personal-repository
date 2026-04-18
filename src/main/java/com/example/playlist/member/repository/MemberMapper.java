package com.example.playlist.member.repository;

import com.example.playlist.member.entity.Gender;
import com.example.playlist.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface MemberMapper {

    Optional<Member> findById(Long memberId);
    Optional<Member> findByEmail(String email);
    void save(Member member);
    Optional<Member> findByLoginId(String loginId);
    void updateProfile(@Param("memberId") Long memberId,
                       @Param("nickname") String nickname,
                       @Param("age") int age,
                       @Param("gender") Gender gender);
}
