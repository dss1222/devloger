package com.devloger.authservice.service;

import com.devloger.authservice.domain.User;
import com.devloger.authservice.dto.UserSignupRequest;
import com.devloger.authservice.dto.UserLoginRequest;
import com.devloger.authservice.dto.UserLoginResponse;
import com.devloger.authservice.dto.UserMeResponse;
import com.devloger.authservice.exception.CustomException;
import com.devloger.authservice.exception.ErrorCode;
import com.devloger.authservice.repository.UserRepository;
import com.devloger.authservice.util.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtProvider jwtProvider;

    private UserService userService;
    private PasswordEncoder passwordEncoder;

    // 테스트 데이터
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "testuser";
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_TOKEN = "test-jwt-token";

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder, jwtProvider);
    }

    @Nested
    @DisplayName("회원가입 서비스 테스트")
    class SignupTest {

        @Test
        @DisplayName("회원가입 성공")
        void 회원가입_성공() {
            // given
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);

            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
            when(userRepository.findByNickname(TEST_NICKNAME)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            User result = userService.signup(request);

            // then
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(passwordEncoder.matches(TEST_PASSWORD, result.getPassword())).isTrue();
            assertThat(result.getNickname()).isEqualTo(TEST_NICKNAME);

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 중복 이메일")
        void 회원가입_실패_중복_이메일() {
            // given
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mock(User.class)));

            // when
            Throwable e = catchThrowable(() -> userService.signup(request));

            // then
            assertCustomException(e, ErrorCode.DUPLICATED_EMAIL);
        }

        @Test
        @DisplayName("회원가입 실패 - 중복 닉네임")
        void 회원가입_실패_중복_닉네임() {
            // given
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
            when(userRepository.findByNickname(TEST_NICKNAME)).thenReturn(Optional.of(mock(User.class)));

            // when
            Throwable e = catchThrowable(() -> userService.signup(request));

            // then
            assertCustomException(e, ErrorCode.DUPLICATED_NICKNAME);
        }

        @Test
        @DisplayName("회원가입 실패 - 유효하지 않은 이메일 형식")
        void 회원가입_실패_유효하지_않은_이메일_형식() {
            // given
            UserSignupRequest request = createSignupRequest("invalid-email", TEST_PASSWORD, TEST_NICKNAME);

            // when
            Throwable e = catchThrowable(() -> userService.signup(request));

            // then
            assertCustomException(e, ErrorCode.INVALID_EMAIL);
        }

        @Test
        @DisplayName("회원가입 실패 - 비밀번호 길이 부족")
        void 회원가입_실패_비밀번호_길이_부족() {
            // given
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, "123", TEST_NICKNAME);

            // when
            Throwable e = catchThrowable(() -> userService.signup(request));

            // then
            assertCustomException(e, ErrorCode.INVALID_PASSWORD);
        }

        @Test
        @DisplayName("회원가입 실패 - 닉네임 길이 부족")
        void 회원가입_실패_닉네임_길이_부족() {
            // given
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, "a");

            // when
            Throwable e = catchThrowable(() -> userService.signup(request));

            // then
            assertCustomException(e, ErrorCode.INVALID_NICKNAME);
        }
    }

    @Nested
    @DisplayName("로그인 서비스 테스트")
    class LoginTest {

        @Test
        @DisplayName("로그인 성공")
        void 로그인_성공() {
            // given
            UserLoginRequest request = createLoginRequest(TEST_EMAIL, TEST_PASSWORD);
            String encodedPassword = passwordEncoder.encode(TEST_PASSWORD);
            User user = createUser(TEST_USER_ID, TEST_EMAIL, encodedPassword, TEST_NICKNAME);

            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
            when(jwtProvider.createToken(TEST_USER_ID, TEST_EMAIL, TEST_NICKNAME)).thenReturn(TEST_TOKEN);

            // when
            UserLoginResponse response = userService.login(request);

            // then
            assertThat(response.id()).isEqualTo(TEST_USER_ID);
            assertThat(response.email()).isEqualTo(TEST_EMAIL);
            assertThat(response.nickname()).isEqualTo(TEST_NICKNAME);
            assertThat(response.accessToken()).isEqualTo(TEST_TOKEN);

            verify(userRepository).findByEmail(TEST_EMAIL);
            verify(jwtProvider).createToken(TEST_USER_ID, TEST_EMAIL, TEST_NICKNAME);
        }

        @Test
        @DisplayName("로그인 실패 - 존재하지 않는 사용자")
        void 로그인_실패_존재하지_않는_사용자() {
            // given
            UserLoginRequest request = createLoginRequest(TEST_EMAIL, TEST_PASSWORD);
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

            // when
            Throwable e = catchThrowable(() -> userService.login(request));

            // then
            assertCustomException(e, ErrorCode.USER_NOT_FOUND);
            verify(userRepository).findByEmail(TEST_EMAIL);
            verifyNoInteractions(jwtProvider);
        }

        @Test
        @DisplayName("로그인 실패 - 잘못된 비밀번호")
        void 로그인_실패_잘못된_비밀번호() {
            // given
            UserLoginRequest request = createLoginRequest(TEST_EMAIL, "wrong-password");
            String encodedPassword = passwordEncoder.encode(TEST_PASSWORD);
            User user = createUser(TEST_USER_ID, TEST_EMAIL, encodedPassword, TEST_NICKNAME);

            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

            // when
            Throwable e = catchThrowable(() -> userService.login(request));

            // then
            assertCustomException(e, ErrorCode.INVALID_CREDENTIALS);
            verify(userRepository).findByEmail(TEST_EMAIL);
            verifyNoInteractions(jwtProvider);
        }
    }

    @Nested
    @DisplayName("내 정보 조회 서비스 테스트")
    class GetMeTest {

        @Test
        @DisplayName("내 정보 조회 성공")
        void 내_정보_조회_성공() {
            // given
            User user = createUser(TEST_USER_ID, TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

            // when
            UserMeResponse response = userService.getById(TEST_USER_ID);

            // then
            assertThat(response.id()).isEqualTo(TEST_USER_ID);
            assertThat(response.email()).isEqualTo(TEST_EMAIL);
            assertThat(response.nickname()).isEqualTo(TEST_NICKNAME);
            verify(userRepository).findById(TEST_USER_ID);
        }

        @Test
        @DisplayName("내 정보 조회 실패 - 존재하지 않는 사용자")
        void 내_정보_조회_실패_존재하지_않는_사용자() {
            // given
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            // when
            Throwable e = catchThrowable(() -> userService.getById(TEST_USER_ID));

            // then
            assertCustomException(e, ErrorCode.USER_NOT_FOUND);
            verify(userRepository).findById(TEST_USER_ID);
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

    private void assertCustomException(Throwable e, ErrorCode expectedCode) {
        assertThat(e).isInstanceOf(CustomException.class);
        assertThat(((CustomException) e).getErrorCode()).isEqualTo(expectedCode);
    }
}
