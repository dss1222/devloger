package com.devloger.postservice.controller;

import com.devloger.postservice.dto.PostCreateRequest;
import com.devloger.postservice.dto.PostCreateResponse;
import com.devloger.postservice.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    // API 엔드포인트
    private static final String CREATE_URL = "/posts";

    // 테스트 데이터
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_TITLE = "테스트 제목";
    private static final String TEST_CONTENT = "테스트 본문입니다.";

    @Nested
    @DisplayName("게시글 작성 API 테스트")
    class CreateTest {

        @Test
        @DisplayName("게시글 작성 성공")
        void 게시글_작성_성공() throws Exception {
            // given
            PostCreateRequest request = createPostRequest(TEST_TITLE, TEST_CONTENT);
            PostCreateResponse response = new PostCreateResponse(1L, TEST_TITLE, TEST_CONTENT, TEST_USER_ID, LocalDateTime.now());

            when(postService.create(any(), any())).thenReturn(response);

            // when & then
            performCreateRequest(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value(TEST_TITLE))
                    .andExpect(jsonPath("$.content").value(TEST_CONTENT))
                    .andExpect(jsonPath("$.userId").value(TEST_USER_ID));
        }

        private ResultActions performCreateRequest(PostCreateRequest request) throws Exception {
            return mockMvc.perform(post(CREATE_URL)
                    .header("X-User-Id", TEST_USER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("게시글 작성 실패 - 제목 누락")
        void 게시글_작성_실패_제목_누락() throws Exception {
            // given
            PostCreateRequest request = new PostCreateRequest(null, TEST_CONTENT);
    
            // when & then
            performCreateRequest(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("게시글 작성 실패 - 내용 누락")
        void 게시글_작성_실패_내용_누락() throws Exception {
            // given
            PostCreateRequest request = new PostCreateRequest(TEST_TITLE, null);

            // when & then
            performCreateRequest(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("게시글 작성 실패 - X-User-Id 헤더 누락")
        void 게시글_작성_실패_헤더_누락() throws Exception {
            // given
            PostCreateRequest request = createPostRequest(TEST_TITLE, TEST_CONTENT);

            // when & then
            mockMvc.perform(post(CREATE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }


    }



    


    // 테스트 요청 생성 메서드
    private PostCreateRequest createPostRequest(String title, String content) {
        return new PostCreateRequest(title, content);
    }
}
