package com.devloger.postservice.service;

import com.devloger.postservice.domain.Post;
import com.devloger.postservice.dto.PostCreateRequest;
import com.devloger.postservice.dto.PostCreateResponse;
import com.devloger.postservice.dto.PostSummaryResponse;
import com.devloger.postservice.dto.PostDetailResponse;
import com.devloger.postservice.dto.PostUpdateRequest;
import com.devloger.postservice.repository.PostRepository;
import com.devloger.postservice.exception.CustomException;
import com.devloger.postservice.exception.ErrorCode;
import com.devloger.postservice.util.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
            PostCreateRequest request = TestDataUtil.createPostRequest(TestDataUtil.TEST_TITLE, TestDataUtil.TEST_CONTENT);
            Post savedPost = TestDataUtil.createPost(
                TestDataUtil.TEST_POST_ID, TestDataUtil.TEST_TITLE, TestDataUtil.TEST_CONTENT, TestDataUtil.TEST_USER_ID, TestDataUtil.TEST_CREATED_AT
            );
            
            when(postRepository.save(any(Post.class))).thenReturn(savedPost);

            // when
            PostCreateResponse response = postService.create(request, TestDataUtil.TEST_USER_ID);

            // then
            assertThat(response.id()).isEqualTo(TestDataUtil.TEST_POST_ID);
            assertThat(response.title()).isEqualTo(TestDataUtil.TEST_TITLE);
            assertThat(response.content()).isEqualTo(TestDataUtil.TEST_CONTENT);
            assertThat(response.userId()).isEqualTo(TestDataUtil.TEST_USER_ID);
            assertThat(response.createdAt()).isEqualTo(TestDataUtil.TEST_CREATED_AT);

            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("게시글 생성 실패 - 제목이 null인 경우")
        void 게시글_생성_실패_제목_null() {
            // given
            PostCreateRequest request = TestDataUtil.createPostRequest(null, TestDataUtil.TEST_CONTENT);

            // when & then
            assertThatThrownBy(() -> postService.create(request, TestDataUtil.TEST_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 필수입니다.");
        }

        @Test
        @DisplayName("게시글 생성 실패 - 내용이 null인 경우")
        void 게시글_생성_실패_내용_null() {
            // given
            PostCreateRequest request = TestDataUtil.createPostRequest(TestDataUtil.TEST_TITLE, null);

            // when & then
            assertThatThrownBy(() -> postService.create(request, TestDataUtil.TEST_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("내용은 필수입니다.");
        }

        @Test
        @DisplayName("게시글 생성 실패 - 제목이 빈 문자열인 경우")
        void 게시글_생성_실패_제목_빈문자열() {
            // given
            PostCreateRequest request = TestDataUtil.createPostRequest("", TestDataUtil.TEST_CONTENT);

            // when & then
            assertThatThrownBy(() -> postService.create(request, TestDataUtil.TEST_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 필수입니다.");
        }

        @Test
        @DisplayName("게시글 생성 실패 - 내용이 빈 문자열인 경우")
        void 게시글_생성_실패_내용_빈문자열() {
            // given
            PostCreateRequest request = TestDataUtil.createPostRequest(TestDataUtil.TEST_TITLE, "");

            // when & then
            assertThatThrownBy(() -> postService.create(request, TestDataUtil.TEST_USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("내용은 필수입니다.");
        }
    }

    @Nested
    @DisplayName("게시글 목록 조회 서비스 테스트")
    class GetPostsTest {

        @Test
        @DisplayName("게시글 목록 조회 성공")
        void 게시글_목록_조회_성공() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Post> posts = List.of(
                TestDataUtil.createPost(1L, "제목1", "내용1", 1L, TestDataUtil.TEST_CREATED_AT),
                TestDataUtil.createPost(2L, "제목2", "내용2", 2L, TestDataUtil.TEST_CREATED_AT)
            );
            Page<Post> postPage = new PageImpl<>(posts, pageable, 2);
            
            when(postRepository.findAll(pageable)).thenReturn(postPage);

            // when
            Page<PostSummaryResponse> result = postService.getPosts(pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.isLast()).isTrue();
            
            PostSummaryResponse firstPost = result.getContent().get(0);
            assertThat(firstPost.id()).isEqualTo(1L);
            assertThat(firstPost.title()).isEqualTo("제목1");
            assertThat(firstPost.content()).isEqualTo("내용1");
            assertThat(firstPost.userId()).isEqualTo(1L);
            assertThat(firstPost.createdAt()).isEqualTo(TestDataUtil.TEST_CREATED_AT);

            verify(postRepository).findAll(pageable);
        }

        @Test
        @DisplayName("게시글 목록 조회 성공 - 빈 결과")
        void 게시글_목록_조회_성공_빈결과() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            
            when(postRepository.findAll(pageable)).thenReturn(emptyPage);

            // when
            Page<PostSummaryResponse> result = postService.getPosts(pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            assertThat(result.isLast()).isTrue();

            verify(postRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("게시글 상세 조회 서비스 테스트")
    class GetPostDetailTest {

        @Test
        @DisplayName("게시글 상세 조회 성공")
        void 게시글_상세_조회_성공() {
            // given
            Post post = TestDataUtil.createPost(
                TestDataUtil.TEST_POST_ID, TestDataUtil.TEST_TITLE, TestDataUtil.TEST_CONTENT, TestDataUtil.TEST_USER_ID, TestDataUtil.TEST_CREATED_AT
            );
            when(postRepository.findById(TestDataUtil.TEST_POST_ID)).thenReturn(java.util.Optional.of(post));

            // when
            PostDetailResponse result = postService.getPostById(TestDataUtil.TEST_POST_ID);

            // then
            assertThat(result.id()).isEqualTo(TestDataUtil.TEST_POST_ID);
            assertThat(result.title()).isEqualTo(TestDataUtil.TEST_TITLE);
            assertThat(result.content()).isEqualTo(TestDataUtil.TEST_CONTENT);
            assertThat(result.userId()).isEqualTo(TestDataUtil.TEST_USER_ID);
            assertThat(result.createdAt()).isEqualTo(TestDataUtil.TEST_CREATED_AT);

            verify(postRepository).findById(TestDataUtil.TEST_POST_ID);
        }

        @Test
        @DisplayName("게시글 상세 조회 실패 - 존재하지 않는 ID")
        void 게시글_상세_조회_실패_존재하지_않는_ID() {
            // given
            Long invalidId = 999L;
            when(postRepository.findById(invalidId)).thenReturn(java.util.Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.getPostById(invalidId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());

            verify(postRepository).findById(invalidId);
        }
    }

    @Nested
    @DisplayName("게시글 수정 서비스 테스트")
    class UpdatePostTest {

        @Test
        @DisplayName("게시글 수정 성공")
        void 게시글_수정_성공() {
            // given
            Post post = TestDataUtil.createPost(
                TestDataUtil.TEST_POST_ID, TestDataUtil.TEST_TITLE, TestDataUtil.TEST_CONTENT, TestDataUtil.TEST_USER_ID, TestDataUtil.TEST_CREATED_AT
            );
            PostUpdateRequest request = new PostUpdateRequest("수정된 제목", "수정된 내용");

            when(postRepository.findById(TestDataUtil.TEST_POST_ID)).thenReturn(java.util.Optional.of(post));
            when(postRepository.save(any(Post.class))).thenReturn(post);

            // when
            PostDetailResponse result = postService.update(TestDataUtil.TEST_POST_ID, TestDataUtil.TEST_USER_ID, request);

            // then
            assertThat(result.title()).isEqualTo("수정된 제목");
            assertThat(result.content()).isEqualTo("수정된 내용");
            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("게시글 수정 실패 - 작성자가 아님")
        void 게시글_수정_실패_권한없음() {
            // given
            Post post = TestDataUtil.createPost(
                TestDataUtil.TEST_POST_ID, TestDataUtil.TEST_TITLE, TestDataUtil.TEST_CONTENT, TestDataUtil.TEST_USER_ID, TestDataUtil.TEST_CREATED_AT
            );
            Long otherUserId = 2L;
            PostUpdateRequest request = new PostUpdateRequest("수정된 제목", "수정된 내용");

            when(postRepository.findById(TestDataUtil.TEST_POST_ID)).thenReturn(java.util.Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postService.update(TestDataUtil.TEST_POST_ID, otherUserId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.UNAUTHORIZED_ACCESS.getMessage());
        }

        @Test
        @DisplayName("게시글 수정 실패 - 존재하지 않는 게시글")
        void 게시글_수정_실패_존재하지_않음() {
            // given
            PostUpdateRequest request = new PostUpdateRequest("수정된 제목", "수정된 내용");

            when(postRepository.findById(TestDataUtil.TEST_POST_ID)).thenReturn(java.util.Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.update(TestDataUtil.TEST_POST_ID, TestDataUtil.TEST_USER_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.POST_NOT_FOUND.getMessage());
        }
    }
} 