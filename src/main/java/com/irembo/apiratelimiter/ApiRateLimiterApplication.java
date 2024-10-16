package com.irembo.apiratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(excludeName = {"org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration"})
@EnableCaching
@EnableAsync
public class ApiRateLimiterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiRateLimiterApplication.class, args);
    }

}
