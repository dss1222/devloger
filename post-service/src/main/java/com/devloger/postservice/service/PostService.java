package com.devloger.postservice.service;

import com.devloger.postservice.domain.Post;
import com.devloger.postservice.dto.PostCreateRequest;
import com.devloger.postservice.dto.PostCreateResponse;
import com.devloger.postservice.dto.PostSummaryResponse;
import com.devloger.postservice.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public PostCreateResponse create(PostCreateRequest request, Long userId) {
        validateRequest(request);
        
        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .userId(userId)
                .build();

        Post saved = postRepository.save(post);

        return new PostCreateResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getContent(),
                saved.getUserId(),
                saved.getCreatedAt()
        );
    }

    public Page<PostSummaryResponse> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(PostSummaryResponse::from);
    }

    private void validateRequest(PostCreateRequest request) {
        if (!StringUtils.hasText(request.title())) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (!StringUtils.hasText(request.content())) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
    }
}
