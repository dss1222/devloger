package com.devloger.postservice.service;

import com.devloger.postservice.domain.Post;
import com.devloger.postservice.dto.PostCreateRequest;
import com.devloger.postservice.dto.PostCreateResponse;
import com.devloger.postservice.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    private PostService postService;

    // 테스트 데이터
    private static final String TEST_TITLE = "테스트 제목";
    private static final String TEST_CONTENT = "테스트 내용";
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_POST_ID = 1L;
    private static final LocalDateTime TEST_CREATED_AT = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository);
    }

    @Nested
    @DisplayName("게시글 생성 서비스 테스트")
    class CreateTest {

        @Test
        @DisplayName("게시글 생성 성공")
        void 게시글_생성_성공() {
            // given
            PostCreateRequest request = createPostRequest(TEST_TITLE, TEST_CONTENT);
            Post savedPost = createPost(TEST_POST_ID, TEST_TITLE, TEST_CONTENT, TEST_USER_ID, TEST_CREATED_AT);
            
            when(postRepository.save(any(Post.class))).thenReturn(savedPost);

            // when
            PostCreateResponse response = postService.create(request, TEST_USER_ID);

            // then
            assertThat(response.id()).isEqualTo(TEST_POST_ID);
            assertThat(response.title()).isEqualTo(TEST_TITLE);
            assertThat(response.content()).isEqualTo(TEST_CONTENT);
            assertThat(response.userId()).isEqualTo(TEST_USER_ID);
            assertThat(response.createdAt()).isEqualTo(TEST_CREATED_AT);

            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("게시글 생성 실패 - 제목이 null인 경우")
        void 게시글_생성_실패_제목_null() {
            // given
            PostCreateRequest request = createPostRequest(null, TEST_CONTENT);

            // when & then
            assertThatThrownBy(() -> postService.create(request, TEST_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 필수입니다.");
        }

        @Test
        @DisplayName("게시글 생성 실패 - 내용이 null인 경우")
        void 게시글_생성_실패_내용_null() {
            // given
            PostCreateRequest request = createPostRequest(TEST_TITLE, null);

            // when & then
            assertThatThrownBy(() -> postService.create(request, TEST_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("내용은 필수입니다.");
        }

        @Test
        @DisplayName("게시글 생성 실패 - 제목이 빈 문자열인 경우")
        void 게시글_생성_실패_제목_빈문자열() {
            // given
            PostCreateRequest request = createPostRequest("", TEST_CONTENT);

            // when & then
            assertThatThrownBy(() -> postService.create(request, TEST_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 필수입니다.");
        }

        @Test
        @DisplayName("게시글 생성 실패 - 내용이 빈 문자열인 경우")
        void 게시글_생성_실패_내용_빈문자열() {
            // given
            PostCreateRequest request = createPostRequest(TEST_TITLE, "");

            // when & then
            assertThatThrownBy(() -> postService.create(request, TEST_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("내용은 필수입니다.");
        }
    }

    // 테스트 데이터 생성 메서드
    private PostCreateRequest createPostRequest(String title, String content) {
        return new PostCreateRequest(title, content);
    }

    private Post createPost(Long id, String title, String content, Long userId, LocalDateTime createdAt) {
        return Post.builder()
                .id(id)
                .title(title)
                .content(content)
                .userId(userId)
                .createdAt(createdAt)
                .build();
    }
} 