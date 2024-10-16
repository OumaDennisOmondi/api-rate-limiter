package com.irembo.apiratelimiter.repos;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SetRepository {

    private static final String CACHE_NAME = "set-values";

    private final StringRedisTemplate redisTemplate;

    public SetRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Cacheable(CACHE_NAME)
    public Set<String> getItems(String key) {
        return redisTemplate.opsForSet().members(key);
    }


    @CacheEvict(cacheNames = CACHE_NAME, key = "#p0")
    public void addItem(String key, String value, Duration expire) {
        redisTemplate.opsForSet().add(key, value);
        redisTemplate.expire(key, expire.getSeconds(), TimeUnit.SECONDS);
    }
}
