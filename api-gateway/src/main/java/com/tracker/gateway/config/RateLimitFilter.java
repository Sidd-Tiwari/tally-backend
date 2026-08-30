package com.tracker.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed-window rate limiter, keyed by the caller's bearer token if present
 * (so each user/API key gets their own budget) or by IP address otherwise.
 *
 * This is intentionally simple: an in-memory counter that resets every
 * minute. It protects a single free-tier instance from casual abuse. It
 * is NOT distributed - if you ever run more than one gateway instance,
 * replace this with a Redis-backed limiter (Spring Cloud Gateway ships
 * one out of the box: RequestRateLimiter + ReactiveRedisTemplate).
 */
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Value("${rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${rate-limit.auth-requests-per-minute:10}")
    private int authRequestsPerMinute;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    private record Window(long windowStartMillis, AtomicInteger count) {}

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        boolean isAuthEndpoint = path.startsWith("/api/auth/register") || path.startsWith("/api/auth/login");

        String key = clientKey(request);
        int limit = isAuthEndpoint ? authRequestsPerMinute : requestsPerMinute;

        long now = System.currentTimeMillis();
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStartMillis() > 60_000) {
                return new Window(now, new AtomicInteger(1));
            }
            existing.count().incrementAndGet();
            return existing;
        });

        if (window.count().get() > limit) {
            return tooManyRequests(exchange, limit);
        }

        return chain.filter(exchange);
    }

    private String clientKey(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Bucket by token, not by decoded userId, to avoid parsing JWTs
            // twice (JwtAuthenticationFilter already validates it downstream).
            return "token:" + authHeader.substring(7).hashCode();
        }
        String ip = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        return "ip:" + ip;
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange, int limit) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        byte[] bytes = ("{\"error\":\"Rate limit exceeded (" + limit + " requests/min). Try again shortly.\"}")
                .getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -2; // run before JWT auth filter so abusive traffic is rejected earliest
    }
}
