package com.irembo.apiratelimiter.repos;

import java.time.Duration;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class KeyValueRepository {

    private static final String CACHE_NAME = "key-value";

    private final StringRedisTemplate redisTemplate;

    public KeyValueRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Cacheable(CACHE_NAME)
    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#p0")
    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#p0")
    public void setValue(String key, String value, Duration expire) {
        redisTemplate.opsForValue().set(key, value, expire);
    }
}
