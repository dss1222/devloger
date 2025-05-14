package com.devloger.apigateway.filter;

import com.devloger.apigateway.config.JwtConfig;
import com.devloger.apigateway.dto.ErrorResponse;
import com.devloger.apigateway.exception.JwtErrorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtConfig jwtConfig;

    // ✅ 인증 없이 허용할 경로들 (모든 HTTP 메서드 허용)
    private static final List<String> ANY_WHITELIST = List.of(
        "/auth/login",
        "/auth/signup"
    );

    // ✅ GET 메서드만 허용할 경로들 (ex. /posts 목록)
    private static final List<String> GET_WHITELIST = List.of(
        "/posts"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        boolean isAnyWhitelisted = ANY_WHITELIST.contains(path);
        boolean isGetWhitelisted = GET_WHITELIST.contains(path) && method.equals("GET");

        // ✅ 인증 없이 허용할 경로는 필터 통과
        if ((path.startsWith("/auth") && !path.equals("/auth/me")) || isAnyWhitelisted || isGetWhitelisted) {
            log.info("✅ 필터 패스 - 인증 필요 없음 (whitelisted)");
            return chain.filter(exchange);
        }

        // ✅ JWT Authorization 헤더 확인
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange, JwtErrorType.GENERAL);
        }

        String token = authHeader.replace("Bearer ", "");
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();

            String userId = String.valueOf(claims.get("userId"));

            log.info("JWT 유효성 통과 - userId: {}", userId);

            ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder
                    .header("X-User-Id", userId)
                )
                .build();

            return chain.filter(mutatedExchange);

        } catch (ExpiredJwtException e) {
            return unauthorizedResponse(exchange, JwtErrorType.EXPIRED);
        } catch (UnsupportedJwtException e) {
            return unauthorizedResponse(exchange, JwtErrorType.UNSUPPORTED);
        } catch (MalformedJwtException e) {
            return unauthorizedResponse(exchange, JwtErrorType.MALFORMED);
        } catch (SignatureException e) {
            return unauthorizedResponse(exchange, JwtErrorType.SIGNATURE);
        } catch (JwtException e) {
            return unauthorizedResponse(exchange, JwtErrorType.GENERAL);
        }
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, JwtErrorType errorType) {
        log.warn("JWT 검증 실패: {}", errorType.getMessage());

        exchange.getResponse().setStatusCode(errorType.getStatus());
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse response = new ErrorResponse(errorType.getCode(), errorType.getMessage());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(response);

            DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(json.getBytes(StandardCharsets.UTF_8));

            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
