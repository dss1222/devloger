package com.devloger.authservice.controller;

import com.devloger.authservice.util.JwtProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if ("test@example.com".equals(request.getEmail()) && "1234".equals(request.getPassword())) {
            String token = jwtProvider.createToken("user-1");
            return ResponseEntity.ok(new LoginResponse(token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @Getter
    static class LoginRequest {
        private String email;
        private String password;
    }

    @Getter
    @AllArgsConstructor
    static class LoginResponse {
        private String token;
    }
}
