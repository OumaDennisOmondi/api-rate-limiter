package com.irembo.apiratelimiter.checker;

import com.irembo.apiratelimiter.repos.EventsPublisher;
import com.irembo.apiratelimiter.repos.KeyCounterRepository;
import com.irembo.apiratelimiter.repos.KeyValueRepository;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class UsageCounter {

    private static final String SOFT_LIMIT_MARKER = "1";
    private static final  String SOFT_LIMIT_TOPIC = "soft_limit_topic";
    private final KeyCounterRepository KeyCounterRepository;
    private final KeyValueRepository keyValue;
    private final EventsPublisher eventsPublisher;

    public UsageCounter(KeyCounterRepository KeyCounterRepository, KeyValueRepository keyValue,
            EventsPublisher eventsPublisher) {
        this.KeyCounterRepository = KeyCounterRepository;
        this.keyValue = keyValue;
        this.eventsPublisher = eventsPublisher;
    }

    public long countUsage(String key, Duration duration, Long softLimit) {
        Duration expire = duration.multipliedBy(2);
        long count = KeyCounterRepository.increment(key, expire);

        if(softLimit>0 && count>=softLimit){
            String softLimitKey = key+":soft_limit";
            if(!SOFT_LIMIT_MARKER.equals(keyValue.getValue(softLimitKey))){
                eventsPublisher.publish(key+":"+softLimit,SOFT_LIMIT_TOPIC);
                keyValue.setValue(softLimitKey,SOFT_LIMIT_MARKER,expire);
            }
        }
        return  count;
    }

}
