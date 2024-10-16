package com.irembo.apiratelimiter.limiter;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service("apiKeyResolver")
public class ApiKeyResolver implements KeyResolver {

    private static final String USER_ID_PARAM = "user_id";

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getQueryParams().getFirst(USER_ID_PARAM);
        if (userId == null || userId.isEmpty()) {
            return Mono.empty();
        }
        return Mono.just(userId);
    }
}
