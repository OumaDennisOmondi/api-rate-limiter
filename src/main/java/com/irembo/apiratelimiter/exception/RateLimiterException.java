package com.irembo.apiratelimiter.exception;

public class RateLimiterException extends Exception {
    public RateLimiterException(String message, Throwable cause) {
        super(message, cause);
    }
}
