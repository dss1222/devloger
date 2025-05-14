package com.devloger.authservice.controller;

import com.devloger.authservice.domain.User;
import com.devloger.authservice.dto.UserSignupRequest;
import com.devloger.authservice.dto.UserSignupResponse;
import com.devloger.authservice.dto.UserLoginRequest;
import com.devloger.authservice.dto.UserLoginResponse;
import com.devloger.authservice.dto.UserMeResponse;
import com.devloger.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello, World!!");
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 회원가입 진행")
    public ResponseEntity<UserSignupResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        User user = userService.signup(request);
        UserSignupResponse response = UserSignupResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인 후 JWT 토큰 발급")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "JWT 인증된 사용자의 정보를 반환합니다.")
    public ResponseEntity<UserMeResponse> getMe(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }
}
