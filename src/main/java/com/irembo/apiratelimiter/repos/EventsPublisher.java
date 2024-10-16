package com.irembo.apiratelimiter.repos;

import java.util.Collections;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
public class EventsPublisher {

    private final StringRedisTemplate redisTemplate;


    public EventsPublisher(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Async
    public void publish(String event, String topic) {
        redisTemplate.opsForStream().add(topic, Collections.singletonMap("event", event));
    }
}
