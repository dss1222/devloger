package com.devloger.authservice.service;

import com.devloger.authservice.domain.User;
import com.devloger.authservice.dto.UserSignupRequest;
import com.devloger.authservice.exception.CustomException;
import com.devloger.authservice.exception.ErrorCode;
import com.devloger.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;
    private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "testuser";

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    private UserSignupRequest createSignupRequest(String email, String password, String nickname) {
        return new UserSignupRequest(email, password, nickname);
    }

    private void assertCustomException(Throwable e, ErrorCode expectedCode) {
        assertThat(e).isInstanceOf(CustomException.class);
        assertThat(((CustomException) e).getErrorCode()).isEqualTo(expectedCode);
    }

    @Nested
    class SignupTest {

        @Test
        void 회원가입_성공() {
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);

            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
            when(userRepository.findByNickname(TEST_NICKNAME)).thenReturn(Optional.empty());

            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.signup(request);

            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(passwordEncoder.matches(TEST_PASSWORD, result.getPassword())).isTrue();
            assertThat(result.getNickname()).isEqualTo(TEST_NICKNAME);

            verify(userRepository).save(any(User.class));
        }

        @Test
        void 중복이메일_회원가입_실패() {
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mock(User.class)));

            Throwable e = catchThrowable(() -> userService.signup(request));
            assertCustomException(e, ErrorCode.DUPLICATED_EMAIL);
        }

        @Test
        void 중복닉네임_회원가입_실패() {
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
            when(userRepository.findByNickname(TEST_NICKNAME)).thenReturn(Optional.of(mock(User.class)));

            Throwable e = catchThrowable(() -> userService.signup(request));
            assertCustomException(e, ErrorCode.DUPLICATED_NICKNAME);
        }

        @Test
        void 이메일형식_잘못됨() {
            UserSignupRequest request = createSignupRequest("invalid-email", TEST_PASSWORD, TEST_NICKNAME);
            Throwable e = catchThrowable(() -> userService.signup(request));
            assertCustomException(e, ErrorCode.INVALID_EMAIL);
        }

        @Test
        void 비밀번호길이_부족() {
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, "123", TEST_NICKNAME);
            Throwable e = catchThrowable(() -> userService.signup(request));
            assertCustomException(e, ErrorCode.INVALID_PASSWORD);
        }

        @Test
        void 닉네임길이_부족() {
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, "a");
            Throwable e = catchThrowable(() -> userService.signup(request));
            assertCustomException(e, ErrorCode.INVALID_NICKNAME);
        }
    }
}
