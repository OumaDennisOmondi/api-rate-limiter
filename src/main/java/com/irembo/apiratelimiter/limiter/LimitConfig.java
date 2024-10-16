package com.irembo.apiratelimiter.limiter;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LimitConfig {

    @JsonProperty("l")
    private long limit;
    @JsonProperty("s")
    private long softLimit;
    @JsonProperty("n")
    private int counters = 1;

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getSoftLimit() {
        return softLimit;
    }

    public void setSoftLimit(long softLimit) {
        this.softLimit = softLimit;
    }

    public int getCounters() {
        return counters;
    }

    public void setCounters(int counters) {
        this.counters = counters;
    }
}
