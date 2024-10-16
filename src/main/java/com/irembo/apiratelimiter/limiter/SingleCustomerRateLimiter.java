package com.irembo.apiratelimiter.limiter;

import com.irembo.apiratelimiter.checker.UsageCounter;
import com.irembo.apiratelimiter.model.RateLimit;
import com.irembo.apiratelimiter.util.UsageKeyUtil;
import java.time.Instant;
import java.util.Map;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(2)
public class SingleCustomerRateLimiter extends AbstractTimeBasedRateLimiter {

    private static final String USER_LIMIT_KEY = "user_limit";
    private static final String USAGE_KEY = "user_usage";
    public static final String LIMIT_HEADER = "X-RateLimit-";
    public static final String TOTAL_HEADER = "-Total";
    public static final String USED_HEADER = "-Used";
    public static final String REMAINING_HEADER = "-Remaining";

    private final UsageCounter usageCounter;


    protected SingleCustomerRateLimiter(LimitConfigReader configReader,
            UsageCounter usageCounter) {
        super(configReader);
        this.usageCounter = usageCounter;
    }

    public boolean isAllowed(String userId, RateLimit rateLimit, Map<String, String> headers, Instant now) {
        String timedKey = UsageKeyUtil.timedKey(USAGE_KEY + ":" + userId, rateLimit.getWindow(), now);
        long usage = usageCounter.countUsage(timedKey, rateLimit.getWindow().getDuration(), rateLimit.getSoftLimit());
        long limit = rateLimit.getLimit();
        long remaining = limit - usage;
        headers.put(headerKey(rateLimit, TOTAL_HEADER), Long.toString(limit));
        headers.put(headerKey(rateLimit, USED_HEADER), Long.toString(usage));
        headers.put(headerKey(rateLimit, REMAINING_HEADER), Long.toString(remaining));
        return remaining >= 0;
    }

    @Override
    protected String getConfigKey(String userId) {
        return USER_LIMIT_KEY + ":" + userId;
    }

    @Override
    protected String getDefaultConfigKey() {
        return USER_LIMIT_KEY;
    }

    private static String headerKey(RateLimit rateLimit, String suffix) {
        return LIMIT_HEADER + rateLimit.getWindow().getName() + suffix;
    }

}
