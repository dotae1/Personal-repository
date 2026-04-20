package com.example.playlist.member.repository;

import com.example.playlist.member.entity.MemberSocial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface MemberSocialMapper {

    Optional<MemberSocial> findByProviderAndProviderId(@Param("provider") String provider,
                                                       @Param("providerId") String providerId);

    Optional<MemberSocial> findByMemberIdAndProvider(@Param("memberId") Long memberId,
                                                      @Param("provider") String provider);

    void save(MemberSocial memberSocial);
}