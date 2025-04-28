package com.devloger.postservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private Long userId; // 작성자 ID

    private LocalDateTime createdAt;

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
}
