package com.irembo.apiratelimiter.limiter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.irembo.apiratelimiter.model.RateLimit;
import com.irembo.apiratelimiter.model.TimeWindow;
import com.irembo.apiratelimiter.repos.KeyValueRepository;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LimitConfigReader {

    private final KeyValueRepository keyValue;
    private final ObjectMapper jsonMapper;
    private final MapType mapType;

    public LimitConfigReader(KeyValueRepository keyValue, ObjectMapper jsonMapper) {
        this.keyValue = keyValue;
        this.jsonMapper = jsonMapper;
        mapType = jsonMapper.getTypeFactory().constructMapType(HashMap.class, TimeWindow.class, LimitConfig.class);
    }

    public List<RateLimit> getRateLimits(String key, String defaultKey) throws JsonProcessingException {
        String jsonConfig = keyValue.getValue(key);
        if (jsonConfig == null || jsonConfig.isEmpty()) {
            log.debug("Limit configuration not found for key {}, trying default {}", key, defaultKey);
            if (defaultKey != null && !defaultKey.isEmpty()) {
                jsonConfig = keyValue.getValue(defaultKey);
            }
        }
        if (jsonConfig == null || jsonConfig.isEmpty()) {
            return Collections.emptyList();
        }

        Map<TimeWindow, LimitConfig> configMap = jsonMapper.readValue(jsonConfig, mapType);
        return configMap.entrySet().stream().map(entry -> new RateLimit(entry.getValue().getLimit(),
                entry.getValue().getSoftLimit(), entry.getKey(), entry.getValue().getCounters()))
                .sorted(Comparator.comparing(limit -> ((RateLimit) limit).getWindow().getDuration()).reversed())
                .collect(Collectors.toList());
    }

    public List<RateLimit> getRateLimits(String key) throws JsonProcessingException {
        return getRateLimits(key, "");
    }
}
