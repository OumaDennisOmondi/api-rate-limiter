package com.irembo.apiratelimiter.limiter;

import com.irembo.apiratelimiter.exception.RateLimiterException;
import java.time.Instant;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter.Response;

public interface TimeBasedRateLimiter {

    Response isAllowed(String userId, Instant now) throws RateLimiterException;
}
