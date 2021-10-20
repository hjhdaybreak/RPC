package com.bee.rpc.limiter;

public interface RateLimiter {
    boolean acquire();
}
