package com.devloger.postservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostController {

    @GetMapping("/posts/test")
    public ResponseEntity<String> test(@RequestHeader("X-User-Id") String userId) {
    return ResponseEntity.ok("Hello, " + userId + "님! Post 서비스입니다.");
    }

}
