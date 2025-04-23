package com.devloger.postservice.controller;

import com.devloger.postservice.dto.PostCreateRequest;
import com.devloger.postservice.dto.PostCreateResponse;
import com.devloger.postservice.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

}
