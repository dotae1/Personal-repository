package com.example.playlist.post.service;

import com.example.playlist.member.entity.Member;
import com.example.playlist.member.exception.MemberErrorCode;
import com.example.playlist.member.exception.MemberException;
import com.example.playlist.member.repository.MemberMapper;
import com.example.playlist.post.dto.PostAnswerRequest;
import com.example.playlist.post.dto.PostRequest;
import com.example.playlist.post.dto.PostResponse;
import com.example.playlist.post.entity.InquiryStatus;
import com.example.playlist.post.entity.Post;
import com.example.playlist.post.exception.PostErrorCode;
import com.example.playlist.post.exception.PostException;
import com.example.playlist.post.repository.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostMapper postMapper;
    private final MemberMapper memberMapper;

    public PostResponse createPost(PostRequest request, String loginId) {
        Member member = memberMapper.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Post post = Post.builder()
                .memberId(member.getId())
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .status(InquiryStatus.PENDING)
                .build();

        postMapper.insertPost(post);
        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public PostResponse getPostDetail(Long postId, String loginId) {
        Member member = memberMapper.findByLoginId(loginId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Post post = postMapper.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        if (!post.getMemberId().equals(member.getId())) {
            throw new PostException(PostErrorCode.UNAUTHORIZED_POST_ACCESS);
        }
        return PostResponse.from(post);
    }

    /** 어드민: 전체 문의 목록 */
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postMapper.findAll().stream()
                .map(PostResponse::from)
                .toList();
    }

    /** 어드민: 문의 상세 조회 */
    @Transactional(readOnly = true)
    public PostResponse getAdminPostDetail(Long postId) {
        return PostResponse.from(
                postMapper.findById(postId)
                        .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND))
        );
    }

    /** 어드민: 답변 등록 */
    public void answerPost(Long postId, PostAnswerRequest request) {
        postMapper.findById(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
        postMapper.updateAnswer(postId, request.getAnswer());
    }
}
