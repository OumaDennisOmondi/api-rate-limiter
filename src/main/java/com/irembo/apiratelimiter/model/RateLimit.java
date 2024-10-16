package com.irembo.apiratelimiter.model;

public class RateLimit {

    private final long limit;
    private final long softLimit;
    private final TimeWindow window;
    private final int counterNumber;

    public RateLimit(long limit, long softLimit, TimeWindow window, int counterNumber) {
        this.limit = limit;
        this.softLimit = softLimit;
        this.window = window;
        this.counterNumber = counterNumber;
    }

    public long getLimit() {
        return limit;
    }

    public long getSoftLimit() {
        return softLimit;
    }

    public TimeWindow getWindow() {
        return window;
    }

    public int getCounterNumber() {
        return counterNumber;
    }
}
