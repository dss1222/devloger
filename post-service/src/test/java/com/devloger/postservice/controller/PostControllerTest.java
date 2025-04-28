package com.devloger.postservice.controller;

import com.devloger.postservice.dto.PostCreateRequest;
import com.devloger.postservice.dto.PostCreateResponse;
import com.devloger.postservice.dto.PostDetailResponse;
import com.devloger.postservice.dto.PostSummaryResponse;
import com.devloger.postservice.dto.PostUpdateRequest;
import com.devloger.postservice.service.PostService;
import com.devloger.postservice.exception.CustomException;
import com.devloger.postservice.exception.ErrorCode;
import com.devloger.postservice.util.TestDataUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    private static final String CREATE_URL = "/posts";
    private static final String GET_POSTS_URL = "/posts";

    @Nested
    @DisplayName("게시글 생성 API 테스트")
    class CreateTest {

        @Test
        @DisplayName("게시글 생성 성공")
        void 게시글_생성_성공() throws Exception {
            // given
            PostCreateRequest request = TestDataUtil.createPostRequest(TestDataUtil.TEST_TITLE, TestDataUtil.TEST_CONTENT);
            PostCreateResponse response = TestDataUtil.createPostResponse(
                1L, TestDataUtil.TEST_TITLE, TestDataUtil.TEST_CONTENT, TestDataUtil.TEST_USER_ID, TestDataUtil.TEST_CREATED_AT
            );

            when(postService.create(any(), any())).thenReturn(response);

            // when & then
            performCreateRequest(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value(TestDataUtil.TEST_TITLE))
                    .andExpect(jsonPath("$.content").value(TestDataUtil.TEST_CONTENT))
                    .andExpect(jsonPath("$.userId").value(TestDataUtil.TEST_USER_ID));
        }

        @Test
        @DisplayName("게시글 생성 실패 - 제목 누락")
        void 게시글_생성_실패_제목_누락() throws Exception {
            // given
            PostCreateRequest request = new PostCreateRequest(null, TestDataUtil.TEST_CONTENT);
    
            // when & then
            performCreateRequest(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("게시글 생성 실패 - 내용 누락")
        void 게시글_생성_실패_내용_누락() throws Exception {
            // given
            PostCreateRequest request = new PostCreateRequest(TestDataUtil.TEST_TITLE, null);

            // when & then
            performCreateRequest(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("게시글 생성 실패 - X-User-Id 헤더 누락")
        void 게시글_생성_실패_헤더_누락() throws Exception {
            // given
            PostCreateRequest request = TestDataUtil.createPostRequest(TestDataUtil.TEST_TITLE, TestDataUtil.TEST_CONTENT);

            // when & then
            mockMvc.perform(post(CREATE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("게시글 목록 조회 API 테스트")
    class GetPostsTest {

        @Test
        @DisplayName("게시글 목록 조회 성공")
        void 게시글_목록_조회_성공() throws Exception {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<PostSummaryResponse> posts = List.of(
                TestDataUtil.createPostSummaryResponse(1L, "제목1", "내용1", 1L, TestDataUtil.TEST_CREATED_AT),
                TestDataUtil.createPostSummaryResponse(2L, "제목2", "내용2", 2L, TestDataUtil.TEST_CREATED_AT)
            );
            Page<PostSummaryResponse> postPage = new PageImpl<>(posts, pageable, 2);
            
            when(postService.getPosts(any(Pageable.class))).thenReturn(postPage);

            // when & then
            mockMvc.perform(get(GET_POSTS_URL)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts.length()").value(2))
                .andExpect(jsonPath("$.posts[0].id").value(1L))
                .andExpect(jsonPath("$.posts[0].title").value("제목1"))
                .andExpect(jsonPath("$.posts[0].content").value("내용1"))
                .andExpect(jsonPath("$.posts[0].userId").value(1L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.isLast").value(true));
        }

        @Test
        @DisplayName("게시글 목록 조회 성공 - 빈 결과")
        void 게시글_목록_조회_성공_빈결과() throws Exception {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<PostSummaryResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            
            when(postService.getPosts(any(Pageable.class))).thenReturn(emptyPage);

            // when & then
            mockMvc.perform(get(GET_POSTS_URL)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts.length()").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.isLast").value(true));
        }
    }

    @Nested
    @DisplayName("게시글 상세 조회 API 테스트")
    class GetPostDetailTest {

        @Test
        @DisplayName("게시글 상세 조회 성공")
        void 게시글_상세_조회_성공() throws Exception {
            // given
            Long postId = 1L;
            PostDetailResponse response = TestDataUtil.createPostDetailResponse(
                postId, TestDataUtil.TEST_TITLE, TestDataUtil.TEST_CONTENT, TestDataUtil.TEST_USER_ID, TestDataUtil.TEST_CREATED_AT
            );
            when(postService.getPostById(postId)).thenReturn(response);

            // when & then
            mockMvc.perform(get(GET_POSTS_URL + "/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value(TestDataUtil.TEST_TITLE))
                .andExpect(jsonPath("$.content").value(TestDataUtil.TEST_CONTENT))
                .andExpect(jsonPath("$.userId").value(TestDataUtil.TEST_USER_ID))
                .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        @DisplayName("게시글 상세 조회 실패 - 존재하지 않는 ID")
        void 게시글_상세_조회_실패_존재하지_않는_ID() throws Exception {
            // given
            Long invalidId = 999L;
            when(postService.getPostById(invalidId))
                .thenThrow(new CustomException(ErrorCode.POST_NOT_FOUND));

            // when & then
            mockMvc.perform(get(GET_POSTS_URL + "/" + invalidId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.POST_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value(ErrorCode.POST_NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("게시글 수정 API 테스트")
    class UpdatePostTest {

        @Test
        @DisplayName("게시글 수정 성공")
        void 게시글_수정_성공() throws Exception {
            // given
            Long postId = 1L;
            PostUpdateRequest request = new PostUpdateRequest("수정된 제목", "수정된 내용");
            PostDetailResponse response = TestDataUtil.createPostDetailResponse(
                postId, "수정된 제목", "수정된 내용", TestDataUtil.TEST_USER_ID, TestDataUtil.TEST_CREATED_AT
            );

            when(postService.update(postId, TestDataUtil.TEST_USER_ID, request)).thenReturn(response);

            // when & then
            mockMvc.perform(patch(GET_POSTS_URL + "/" + postId)
                    .header("X-User-Id", TestDataUtil.TEST_USER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.content").value("수정된 내용"))
                .andExpect(jsonPath("$.userId").value(TestDataUtil.TEST_USER_ID));
        }

        @Test
        @DisplayName("게시글 수정 실패 - 작성자가 아닌 경우")
        void 게시글_수정_실패_권한없음() throws Exception {
            // given
            Long postId = 1L;
            Long otherUserId = 2L;
            PostUpdateRequest request = new PostUpdateRequest("수정된 제목", "수정된 내용");

            when(postService.update(postId, otherUserId, request))
                    .thenThrow(new CustomException(ErrorCode.UNAUTHORIZED_ACCESS));

            // when & then
            mockMvc.perform(patch(GET_POSTS_URL + "/" + postId)
                    .header("X-User-Id", otherUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED_ACCESS.name()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED_ACCESS.getMessage()));
        }
    }

    private ResultActions performCreateRequest(PostCreateRequest request) throws Exception {
        return mockMvc.perform(post(CREATE_URL)
                .header("X-User-Id", TestDataUtil.TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
