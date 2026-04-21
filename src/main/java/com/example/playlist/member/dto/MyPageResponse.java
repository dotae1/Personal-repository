package com.example.playlist.member.dto;

import com.example.playlist.member.entity.Gender;
import com.example.playlist.member.entity.Member;
import com.example.playlist.playlist.entity.Playlist;
import com.example.playlist.post.entity.Post;

import java.util.List;

public record MyPageResponse(
        String name,
        String nickname,
        String email,
        Integer age,
        Gender gender,
        List<PlaylistSummaryResponse> playlists,
        List<PostSummaryResponse> posts
) {
    public static MyPageResponse of(Member member, List<Playlist> playlists, List<Post> posts) {
        return new MyPageResponse(
                member.getName(),
                member.getNickname(),
                member.getEmail(),
                member.getAge(),
                member.getGender(),
                playlists.stream().map(PlaylistSummaryResponse::from).toList(),
                posts.stream().map(PostSummaryResponse::from).toList()
        );
    }
}
