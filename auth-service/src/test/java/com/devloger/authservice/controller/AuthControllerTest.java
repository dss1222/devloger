package com.devloger.authservice.controller;

import com.devloger.authservice.domain.User;
import com.devloger.authservice.dto.UserSignupRequest;
import com.devloger.authservice.service.UserService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Nested
    @DisplayName("회원가입 API 테스트")
    class SignupTest {
        private static final String TEST_EMAIL = "test@example.com";
        private static final String TEST_PASSWORD = "password123";
        private static final String TEST_NICKNAME = "testUser";
        private static final String SIGNUP_URL = "/auth/signup";

        @Test
        @DisplayName("회원가입 성공")
        void 회원가입_성공() throws Exception {
            // given
            UserSignupRequest request = new UserSignupRequest(
                    TEST_EMAIL,
                    TEST_PASSWORD,
                    TEST_NICKNAME
            );

            User savedUser = User.builder()
                    .id(1L)
                    .email(TEST_EMAIL)
                    .password("encryptedPassword")
                    .nickname(TEST_NICKNAME)
                    .build();

            given(userService.signup(any(UserSignupRequest.class))).willReturn(savedUser);

            // when
            var result = mockMvc.perform(post(SIGNUP_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.nickname").value(TEST_NICKNAME));
        }

        @Test
        @DisplayName("회원가입 실패 - 이메일 누락")
        void 회원가입_실패_이메일누락() throws Exception {
            // given
            UserSignupRequest request = new UserSignupRequest(
                    null,
                    TEST_PASSWORD,
                    TEST_NICKNAME
            );

            // when
            var result = mockMvc.perform(post(SIGNUP_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("회원가입 실패 - 비밀번호 누락")
        void 회원가입_실패_비밀번호누락() throws Exception {
            // given
            UserSignupRequest request = new UserSignupRequest(
                    TEST_EMAIL,
                    null,
                    TEST_NICKNAME
            );

            // when
            var result = mockMvc.perform(post(SIGNUP_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("회원가입 실패 - 이메일 형식 오류")
        void 회원가입_실패_이메일형식오류() throws Exception {
            // given
            UserSignupRequest request = new UserSignupRequest(
                    "invalid-email",
                    TEST_PASSWORD,
                    TEST_NICKNAME
            );

            // when
            var result = mockMvc.perform(post(SIGNUP_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isBadRequest());
        }
    }
}
