package com.example.playlist.post.controller;

import com.example.playlist.global.success.SuccessResponse;
import com.example.playlist.post.dto.PostAnswerRequest;
import com.example.playlist.post.dto.PostRequest;
import com.example.playlist.post.dto.PostResponse;
import com.example.playlist.post.exception.PostSuccessCode;
import com.example.playlist.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /** 문의 등록 */
    @PostMapping("/posts")
    public ResponseEntity<SuccessResponse<PostResponse>> createPost(
            @RequestBody @Valid PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PostResponse response = postService.createPost(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(PostSuccessCode.POST_CREATED, response));
    }

    /** 내 문의 상세 조회 */
    @GetMapping("/posts/{id}")
    public ResponseEntity<SuccessResponse<PostResponse>> getPostDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PostResponse response = postService.getPostDetail(id, userDetails.getUsername());
        return ResponseEntity.ok(SuccessResponse.of(PostSuccessCode.POST_DETAIL, response));
    }

    /** 어드민: 전체 문의 목록 */
    @GetMapping("/admin/posts")
    public ResponseEntity<SuccessResponse<List<PostResponse>>> getAllPosts() {
        return ResponseEntity.ok(SuccessResponse.of(PostSuccessCode.POST_LIST, postService.getAllPosts()));
    }

    /** 어드민: 문의 상세 조회 */
    @GetMapping("/admin/posts/{id}")
    public ResponseEntity<SuccessResponse<PostResponse>> getAdminPostDetail(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(PostSuccessCode.POST_DETAIL, postService.getAdminPostDetail(id)));
    }

    /** 어드민: 답변 등록 */
    @PutMapping("/admin/posts/{id}/answer")
    public ResponseEntity<SuccessResponse<?>> answerPost(
            @PathVariable Long id,
            @RequestBody @Valid PostAnswerRequest request
    ) {
        postService.answerPost(id, request);
        return ResponseEntity.ok(SuccessResponse.of(PostSuccessCode.POST_ANSWERED));
    }
}
