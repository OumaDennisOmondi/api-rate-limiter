package com.irembo.apiratelimiter.limiter;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service("apiRateLimiter")
@Slf4j
public class ApiRateLimiter implements RateLimiter<EmptyConfig> {

    public static final String FILTER_TIME_HEADER = "X-RateLimit-FilterTime";

    private final List<TimeBasedRateLimiter> rateLimiters;

    public ApiRateLimiter(List<TimeBasedRateLimiter> rateLimiters) {
        this.rateLimiters = rateLimiters;
    }

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        return Mono.just(isAllowed(id));

    }

    private Response isAllowed(String id) {
        Instant now = Instant.now();
        Response response = new Response(true, Collections.emptyMap());
        try {
            for (TimeBasedRateLimiter limiter : rateLimiters) {
                response = limiter.isAllowed(id, now);
                if (!response.isAllowed()) {
                    break;
                }
            }
        } catch (Exception ex) {
            //don't  penalize a customer in case we missed something
            log.error("Failed to check rate limit for user {}", id, ex);
        }
        //add the filter time header
        Map<String, String> headers = new HashMap<>(response.getHeaders());
        headers.put(FILTER_TIME_HEADER, DateTimeFormatter.ISO_INSTANT.format(now));
        return new Response(response.isAllowed(), headers);
    }

    @Override
    public Map<String, EmptyConfig> getConfig() {
        return Collections.emptyMap();
    }

    @Override
    public Class<EmptyConfig> getConfigClass() {
        return EmptyConfig.class;
    }

    @Override
    public EmptyConfig newConfig() {
        return new EmptyConfig();
    }
}
