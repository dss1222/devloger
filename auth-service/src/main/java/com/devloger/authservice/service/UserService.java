package com.devloger.authservice.service;

import com.devloger.authservice.domain.User;
import com.devloger.authservice.dto.UserSignupRequest;
import com.devloger.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MIN_NICKNAME_LENGTH = 2;

    public User signup(UserSignupRequest request) {
        validateEmail(request.email());
        validatePassword(request.password());
        validateNickname(request.nickname());

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.findByNickname(request.nickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        return userRepository.save(user);
    }

    private void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.length() < MIN_NICKNAME_LENGTH) {
            throw new IllegalArgumentException("닉네임은 2자 이상이어야 합니다.");
        }
    }
}
