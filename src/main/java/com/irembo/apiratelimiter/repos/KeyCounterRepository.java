package com.irembo.apiratelimiter.repos;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class KeyCounterRepository {
    private final RedisTemplate redisTemplate;

    public KeyCounterRepository(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public long increment(String key, Duration duration){
        long result = redisTemplate.opsForValue().increment(key);

        redisTemplate.expire(key,duration.getSeconds(),TimeUnit.SECONDS);
        return result;
    }
}
