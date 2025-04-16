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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtConfig jwtConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/auth")) {
            return chain.filter(exchange);
        }

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

            String userId = claims.getSubject();

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
    
        exchange.getResponse().setStatusCode(errorType.getStatus()); // ✅ 상태코드 동적으로 설정
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
    
        ErrorResponse response = new ErrorResponse(errorType.getCode(), errorType.getMessage());
    
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(response);
    
            DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(json.getBytes(StandardCharsets.UTF_8));
    
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return exchange.getResponse().setComplete(); // fallback: 응답 비우고 종료
        }
    }
    

    @Override
    public int getOrder() {
        return -1;
    }
}
