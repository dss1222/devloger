package com.devloger.authservice.service;

import com.devloger.authservice.domain.User;
import com.devloger.authservice.dto.UserSignupRequest;
import com.devloger.authservice.dto.UserLoginRequest;
import com.devloger.authservice.dto.UserLoginResponse;
import com.devloger.authservice.exception.CustomException;
import com.devloger.authservice.exception.ErrorCode;
import com.devloger.authservice.repository.UserRepository;
import com.devloger.authservice.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    // 상수 정의
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MIN_NICKNAME_LENGTH = 2;

    // 의존성 주입
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 회원가입을 처리합니다.
     * 1. 입력값 유효성 검증
     * 2. 중복 체크
     * 3. 비밀번호 암호화
     * 4. 사용자 저장
     *
     * @param request 회원가입 요청 정보
     * @return 저장된 사용자 정보
     */
    public User signup(UserSignupRequest request) {
        // 1. 입력값 유효성 검증
        validateEmail(request.email());
        validatePassword(request.password());
        validateNickname(request.nickname());

        // 2. 중복 체크
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }

        if (userRepository.findByNickname(request.nickname()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_NICKNAME);
        }

        // 3. 비밀번호 암호화 및 사용자 생성
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        // 4. 사용자 저장
        return userRepository.save(user);
    }

    /**
     * 로그인을 처리합니다.
     * 1. 사용자 조회
     * 2. 비밀번호 확인
     * 3. JWT 토큰 발급
     * 4. 응답 반환
     *
     * @param request 로그인 요청 정보
     * @return 로그인 응답 (사용자 정보 + JWT 토큰)
     */
    public UserLoginResponse login(UserLoginRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 3. JWT 토큰 발급
        String token = jwtProvider.createToken(user.getId(), user.getEmail(), user.getNickname());

        // 4. 응답 반환
        return new UserLoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                token
        );
    }

    /**
     * 이메일 유효성을 검증합니다.
     *
     * @param email 검증할 이메일
     * @throws CustomException 이메일이 null이거나 형식이 올바르지 않은 경우
     */
    private void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new CustomException(ErrorCode.INVALID_EMAIL);
        }
    }

    /**
     * 비밀번호 유효성을 검증합니다.
     *
     * @param password 검증할 비밀번호
     * @throws CustomException 비밀번호가 null이거나 최소 길이보다 짧은 경우
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
    }

    /**
     * 닉네임 유효성을 검증합니다.
     *
     * @param nickname 검증할 닉네임
     * @throws CustomException 닉네임이 null이거나 최소 길이보다 짧은 경우
     */
    private void validateNickname(String nickname) {
        if (nickname == null || nickname.length() < MIN_NICKNAME_LENGTH) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME);
        }
    }
}
