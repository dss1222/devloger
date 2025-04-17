package com.devloger.authservice.controller;

import com.devloger.authservice.domain.User;
import com.devloger.authservice.dto.UserSignupRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
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

    @Nested
    @DisplayName("회원가입 API 테스트")
    class SignupTest {
        private static final String EMAIL = "test@example.com";
        private static final String PASSWORD = "password123";
        private static final String NICKNAME = "testUser";

        @Test
        @DisplayName("회원가입 성공")
        void signup_success() throws Exception {
            User user = User.builder()
                    .id(1L)
                    .email(EMAIL)
                    .password("encrypted")
                    .nickname(NICKNAME)
                    .build();

            given(userService.signup(any())).willReturn(user);

            mockMvc.perform(post("/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UserSignupRequest(EMAIL, PASSWORD, NICKNAME))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value(EMAIL))
                    .andExpect(jsonPath("$.nickname").value(NICKNAME));
        }

        @Test
        @DisplayName("회원가입 실패 - 유효하지 않은 이메일 형식")
        void signup_fail_invalid_email() throws Exception {
            UserSignupRequest request = new UserSignupRequest("invalid-email", PASSWORD, NICKNAME);

            mockMvc.perform(post("/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.message").value("must be a well-formed email address"));
        }

        @Test
        @DisplayName("회원가입 실패 - 비밀번호 길이 부족")
        void signup_fail_short_password() throws Exception {
            UserSignupRequest request = new UserSignupRequest(EMAIL, "123", NICKNAME);
            doThrow(new CustomException(ErrorCode.INVALID_PASSWORD))
                    .when(userService)
                    .signup(any());

            mockMvc.perform(post("/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PASSWORD.name()))
                    .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_PASSWORD.getMessage()));
        }
    }
}
