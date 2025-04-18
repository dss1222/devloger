package com.devloger.authservice.controller;

import com.devloger.authservice.domain.User;
import com.devloger.authservice.dto.UserSignupRequest;
import com.devloger.authservice.dto.UserLoginRequest;
import com.devloger.authservice.dto.UserLoginResponse;
import com.devloger.authservice.exception.CustomException;
import com.devloger.authservice.exception.ErrorCode;
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
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // API 엔드포인트
    private static final String SIGNUP_URL = "/auth/signup";
    private static final String LOGIN_URL = "/auth/login";

    // 테스트 데이터
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "testuser";
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_TOKEN = "jwt-token";

    // 유효성 검증 메시지
    private static final String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";
    private static final String EMAIL_NOT_BLANK_MESSAGE = "이메일은 필수 입력값입니다.";
    private static final String PASSWORD_NOT_BLANK_MESSAGE = "비밀번호는 필수 입력값입니다.";
    private static final String INVALID_EMAIL_MESSAGE = "must be a well-formed email address";

    @Nested
    @DisplayName("회원가입 API 테스트")
    class SignupTest {

        @Test
        @DisplayName("회원가입 성공")
        void 회원가입_성공() throws Exception {
            // given
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
            User user = createUser(TEST_USER_ID, TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
            when(userService.signup(any(UserSignupRequest.class))).thenReturn(user);

            // when & then
            performSignupRequest(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                    .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.nickname").value(TEST_NICKNAME));
        }

        @Test
        @DisplayName("회원가입 실패 - 유효하지 않은 이메일 형식")
        void 회원가입_실패_유효하지_않은_이메일_형식() throws Exception {
            // given
            UserSignupRequest request = createSignupRequest("invalid-email", TEST_PASSWORD, TEST_NICKNAME);

            // when & then
            performSignupRequest(request)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(VALIDATION_ERROR_CODE))
                    .andExpect(jsonPath("$.message").value(INVALID_EMAIL_MESSAGE));
        }

        @Test
        @DisplayName("회원가입 실패 - 이메일 누락")
        void 회원가입_실패_이메일_누락() throws Exception {
            // given
            UserSignupRequest request = createSignupRequest(null, TEST_PASSWORD, TEST_NICKNAME);

            // when & then
            performSignupRequest(request)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(VALIDATION_ERROR_CODE))
                    .andExpect(jsonPath("$.message").value(EMAIL_NOT_BLANK_MESSAGE));
        }

        @Test
        @DisplayName("회원가입 실패 - 비밀번호 누락")
        void 회원가입_실패_비밀번호_누락() throws Exception {
            // given
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, null, TEST_NICKNAME);

            // when & then
            performSignupRequest(request)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(VALIDATION_ERROR_CODE))
                    .andExpect(jsonPath("$.message").value(PASSWORD_NOT_BLANK_MESSAGE));
        }

        private ResultActions performSignupRequest(UserSignupRequest request) throws Exception {
            return mockMvc.perform(post(SIGNUP_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }
    }

    @Nested
    @DisplayName("로그인 API 테스트")
    class LoginTest {

        @Test
        @DisplayName("로그인 성공")
        void 로그인_성공() throws Exception {
            // given
            UserLoginRequest request = createLoginRequest(TEST_EMAIL, TEST_PASSWORD);
            UserLoginResponse response = createLoginResponse(TEST_USER_ID, TEST_EMAIL, TEST_NICKNAME, TEST_TOKEN);
            when(userService.login(any(UserLoginRequest.class))).thenReturn(response);

            // when & then
            performLoginRequest(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                    .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.nickname").value(TEST_NICKNAME))
                    .andExpect(jsonPath("$.accessToken").exists());
        }

        @Test
        @DisplayName("로그인 실패 - 이메일 누락")
        void 로그인_실패_이메일_누락() throws Exception {
            // given
            UserLoginRequest request = createLoginRequest(null, TEST_PASSWORD);

            // when & then
            performLoginRequest(request)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(VALIDATION_ERROR_CODE))
                    .andExpect(jsonPath("$.message").value(EMAIL_NOT_BLANK_MESSAGE));
        }

        @Test
        @DisplayName("로그인 실패 - 비밀번호 누락")
        void 로그인_실패_비밀번호_누락() throws Exception {
            // given
            UserLoginRequest request = createLoginRequest(TEST_EMAIL, null);

            // when & then
            performLoginRequest(request)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(VALIDATION_ERROR_CODE))
                    .andExpect(jsonPath("$.message").value(PASSWORD_NOT_BLANK_MESSAGE));
        }

        @Test
        @DisplayName("로그인 실패 - 존재하지 않는 사용자")
        void 로그인_실패_존재하지_않는_사용자() throws Exception {
            // given
            UserLoginRequest request = createLoginRequest(TEST_EMAIL, TEST_PASSWORD);
            when(userService.login(any(UserLoginRequest.class)))
                    .thenThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

            // when & then
            performLoginRequest(request)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.name()));
        }

        @Test
        @DisplayName("로그인 실패 - 잘못된 비밀번호")
        void 로그인_실패_잘못된_비밀번호() throws Exception {
            // given
            UserLoginRequest request = createLoginRequest(TEST_EMAIL, "wrong-password");
            when(userService.login(any(UserLoginRequest.class)))
                    .thenThrow(new CustomException(ErrorCode.INVALID_CREDENTIALS));

            // when & then
            performLoginRequest(request)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_CREDENTIALS.name()));
        }

        private ResultActions performLoginRequest(UserLoginRequest request) throws Exception {
            return mockMvc.perform(post(LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }
    }

    // 테스트 데이터 생성 메서드
    private UserSignupRequest createSignupRequest(String email, String password, String nickname) {
        return new UserSignupRequest(email, password, nickname);
    }

    private UserLoginRequest createLoginRequest(String email, String password) {
        return new UserLoginRequest(email, password);
    }

    private User createUser(Long id, String email, String password, String nickname) {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .nickname(nickname)
                .build();
    }

    private UserLoginResponse createLoginResponse(Long id, String email, String nickname, String token) {
        return new UserLoginResponse(id, email, nickname, token);
    }
}
