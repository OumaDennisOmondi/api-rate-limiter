package com.irembo.apiratelimiter.limiter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.irembo.apiratelimiter.exception.RateLimiterException;
import com.irembo.apiratelimiter.model.RateLimit;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter.Response;

public abstract class AbstractTimeBasedRateLimiter implements TimeBasedRateLimiter {

    private final LimitConfigReader configReader;

    protected AbstractTimeBasedRateLimiter(LimitConfigReader configReader) {
        this.configReader = configReader;
    }

    public Response isAllowed(String userId, Instant now) throws RateLimiterException {
        Map<String, String> headers = new HashMap<>();
        try {
            List<RateLimit> rateLimits = configReader.getRateLimits(getConfigKey(userId), getDefaultConfigKey());
            for (RateLimit rateLimit : rateLimits) {
                if (!isAllowed(userId, rateLimit, headers, now)) {
                    return new Response(false, headers);
                }
            }
            return new Response(true, headers);
        } catch (JsonProcessingException ex) {
            throw new RateLimiterException("Unable to apply Rate Limiter for user:" + userId, ex);
        }
    }

    protected abstract boolean isAllowed(String userId, RateLimit rateLimit, Map<String, String> headers, Instant now);

    protected abstract String getConfigKey(String userId);

    protected abstract String getDefaultConfigKey();
}
