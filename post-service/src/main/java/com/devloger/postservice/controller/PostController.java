package com.devloger.postservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostController {
    @GetMapping("/posts/test")
    public String test() {
        return "Hello World";
    }
}
