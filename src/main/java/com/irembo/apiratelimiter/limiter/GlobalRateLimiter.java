package com.irembo.apiratelimiter.limiter;

import com.irembo.apiratelimiter.checker.UsageCounter;
import com.irembo.apiratelimiter.model.RateLimit;
import com.irembo.apiratelimiter.repos.SetRepository;
import com.irembo.apiratelimiter.util.UsageKeyUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(1)
public class GlobalRateLimiter extends AbstractTimeBasedRateLimiter {

    private static final String GLOBAL_LIMIT_KEY = "global_limit";
    private static final String USAGE_KEY = "global_usage";


    private final UsageCounter usageCounter;
    private final SetRepository setRepository;

    public GlobalRateLimiter(LimitConfigReader configReader,
            UsageCounter usageCounter, SetRepository setRepository) {
        super(configReader);
        this.usageCounter = usageCounter;
        this.setRepository = setRepository;
    }

    protected boolean isAllowed(String userId, RateLimit rateLimit, Map<String, String> headers, Instant now) {
        String timedKey = UsageKeyUtil.timedKey(USAGE_KEY, rateLimit.getWindow(), now);
        String finishedKey = timedKey + "finished_counters";
        int counters = rateLimit.getCounterNumber();
        if (counters <= 0) {
            counters = 1;
        }
        List<Integer> counterIds = activeCounters(finishedKey, counters);
        for (int counterId : counterIds) {
            String counterKey = timedKey + ":" + counterId;
            long counterLimit = counterLimit(counterId, counters, rateLimit.getLimit());
            long usage = usageCounter.countUsage(counterKey, rateLimit.getWindow().getDuration(), 0L);
            if (usage > counterLimit) {
                setRepository.addItem(finishedKey, Integer.toString(counterId),
                        rateLimit.getWindow().getDuration().multipliedBy(2));
            } else {
                return true;
            }
        }

        return false;
    }

    private List<Integer> activeCounters(String finishedKey, int counters) {
        Set<String> finishedCounters = setRepository.getItems(finishedKey);
        if (finishedCounters == null) {
            finishedCounters = Collections.emptySet();
        }
        List<Integer> counterIds = new ArrayList<>();
        for (int counterId = 0; counterId < counters; counterId++) {
            if (!finishedCounters.contains(Integer.toString(counterId))) {
                counterIds.add(counterId);
            }
        }
        //randomly order the counters to ensure that they are evenly incremented
        Collections.shuffle(counterIds);
        return counterIds;
    }

    private long counterLimit(int counterId, int counters, long limit) {
        long share = limit / counters;
        if (counterId < limit % counters) {
            share++;
        }
        return share;
    }

    @Override
    protected String getConfigKey(String userId) {
        return GLOBAL_LIMIT_KEY;
    }

    @Override
    protected String getDefaultConfigKey() {
        return "";
    }
}
