package com.example.playlist.post.repository;

import com.example.playlist.post.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PostMapper {
    void insertPost(Post post);
    Optional<Post> findById(@Param("id") Long id);
    List<Post> findByMemberId(@Param("memberId") Long memberId);
    List<Post> findAll();
    void updateAnswer(@Param("id") Long id, @Param("answer") String answer);
}
