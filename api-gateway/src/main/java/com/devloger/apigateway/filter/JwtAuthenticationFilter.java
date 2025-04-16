package com.devloger.apigateway.filter;

import com.devloger.apigateway.config.JwtConfig;
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
import org.springframework.http.HttpStatus;
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
            log.warn("Missing or invalid Authorization header");
            return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
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
            return unauthorizedResponse(exchange, "Token has expired");
        } catch (UnsupportedJwtException e) {
            return unauthorizedResponse(exchange, "Unsupported JWT");
        } catch (MalformedJwtException e) {
            return unauthorizedResponse(exchange, "Invalid JWT format");
        } catch (SignatureException e) {
            return unauthorizedResponse(exchange, "JWT signature does not match");
        } catch (JwtException e) {
            return unauthorizedResponse(exchange, "JWT validation failed");
        }
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        log.warn("JWT 검증 실패: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format("{\"error\": \"%s\"}", message);
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        DataBuffer buffer = bufferFactory.wrap(json.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
