package com.devloger.authservice.util;

import java.util.Date;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import io.jsonwebtoken.Claims;

@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtProvider {

    private String secret;
    private long expiration;

    public String createToken(Long userId, String email, String nickname) {
        Claims claims = Jwts.claims();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("nickname", nickname);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    // Setter for application.yml 값 매핑
    public void setSecret(String secret) { this.secret = secret; }
    public void setExpiration(long expiration) { this.expiration = expiration; }
}
