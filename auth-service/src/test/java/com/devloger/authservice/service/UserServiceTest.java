package com.devloger.authservice.service;

import com.devloger.authservice.domain.User;
import com.devloger.authservice.dto.UserSignupRequest;
import com.devloger.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    private User createTestUser(String email, String password, String nickname) {
        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .build();
    }

    private UserSignupRequest createSignupRequest(String email, String password, String nickname) {
        return new UserSignupRequest(email, password, nickname);
    }

    @Nested
    class SignupTest {
        @Test
        void 회원가입_성공() {
            // given
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
            User savedUser = createTestUser(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);

            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
            when(userRepository.findByNickname(TEST_NICKNAME)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // when
            User result = userService.signup(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(passwordEncoder.matches(TEST_PASSWORD, result.getPassword())).isTrue();
            assertThat(result.getNickname()).isEqualTo(TEST_NICKNAME);
            
            verify(userRepository).save(any(User.class));
        }

        @Test
        void 회원가입_실패_중복이메일() {
            // given
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
            User existingUser = createTestUser(TEST_EMAIL, "existingPassword", "existingUser");

            when(userRepository.findByEmail(TEST_EMAIL))
                    .thenReturn(Optional.of(existingUser));

            // when & then
            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 사용 중인 이메일입니다.");

            verify(userRepository, never()).save(any());
        }


        @Test
        void 회원가입_실패_중복닉네임() {
            // given
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME);
            User existingUser = createTestUser("existing@example.com", "existingPassword", TEST_NICKNAME);

            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
            when(userRepository.findByNickname(TEST_NICKNAME))
                    .thenReturn(Optional.of(existingUser));

            // when & then
            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 사용 중인 닉네임입니다.");

            verify(userRepository, never()).save(any());
        }


        @Test
        void 회원가입_실패_유효하지않은이메일() {
            // given
            String invalidEmail = "invalid-email";
            UserSignupRequest request = createSignupRequest(invalidEmail, TEST_PASSWORD, TEST_NICKNAME);

            // when & then
            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유효하지 않은 이메일 형식입니다.");
        }


        @Test
        void 회원가입_실패_비밀번호길이부족() {
            // given
            String shortPassword = "123"; // 3자리
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, shortPassword, TEST_NICKNAME);

            // when & then
            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("비밀번호는 최소 8자 이상이어야 합니다.");
        }


        @Test
        void 회원가입_실패_유효하지않은닉네임() {
            // given
            String invalidNickname = "a"; // 너무 짧은 닉네임
            UserSignupRequest request = createSignupRequest(TEST_EMAIL, TEST_PASSWORD, invalidNickname);

            // when & then
            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("닉네임은 2자 이상이어야 합니다.");
        }
    }
}
