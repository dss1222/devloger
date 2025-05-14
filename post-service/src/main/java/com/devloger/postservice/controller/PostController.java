package com.devloger.postservice.controller;

import com.devloger.postservice.dto.PostCreateRequest;
import com.devloger.postservice.dto.PostCreateResponse;
import com.devloger.postservice.dto.PostDetailResponse;
import com.devloger.postservice.dto.PostListResponse;
import com.devloger.postservice.dto.PostSummaryResponse;
import com.devloger.postservice.dto.PostUpdateRequest;
import com.devloger.postservice.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(
        summary = "게시글 작성",
        description = "인증된 사용자가 게시글을 작성합니다. JWT 필터를 통과한 사용자만 호출 가능하며, Gateway에서 X-User-Id 헤더가 추가되어야 합니다."
    )
    public ResponseEntity<PostCreateResponse> create(
        @RequestHeader("X-User-Id") Long userId,
        @Valid @RequestBody PostCreateRequest request
    ) {
        PostCreateResponse response = postService.create(request, userId);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    @Operation(summary = "게시글 목록 조회", description = "전체 게시글을 페이징하여 조회합니다.")
    public ResponseEntity<PostListResponse> getPosts(
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        Page<PostSummaryResponse> page = postService.getPosts(pageable);
        return ResponseEntity.ok(PostListResponse.from(page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 ID를 이용해 상세 내용을 조회합니다.")
    public ResponseEntity<PostDetailResponse> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "게시글 수정", description = "게시글 ID와 수정할 내용을 받아 게시글을 수정합니다. (작성자 본인만 수정 가능)")
    public ResponseEntity<PostDetailResponse> updatePost(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable Long id,
        @Valid @RequestBody PostUpdateRequest request
    ) {
        PostDetailResponse response = postService.update(id, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "게시글 삭제", description = "게시글을 소프트 삭제합니다. 작성자만 삭제 가능")
    public ResponseEntity<Void> deletePost(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable Long id
    ) {
        postService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

}
