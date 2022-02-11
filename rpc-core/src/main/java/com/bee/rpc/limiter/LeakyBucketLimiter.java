package com.bee.rpc.limiter;

import java.util.concurrent.atomic.AtomicInteger;

public class LeakyBucketLimiter implements RateLimiter {

    //桶的容量
    private int capacity = 100;

    //桶中水剩余量
    private AtomicInteger water = new AtomicInteger(0);

    // 水滴的流出的速率 每1000毫秒流出1滴
    private int leakRate;

    // 第一次请求之后,木桶在这个时间点开始漏水
    private long leakTimeStamp;

    public LeakyBucketLimiter(int leakRate) {
        this.leakRate = leakRate;
    }


    @Override
    public boolean acquire() {
        // 如果桶是空的,就当前时间作为桶开是漏出的时间
        if (water.get() == 0) {
            leakTimeStamp = System.currentTimeMillis();
            water.getAndAdd(1);
            return capacity != 0;
        }
        // 先执行漏水,计算剩余水量
        int waterLeft = water.get() - ((int) ((System.currentTimeMillis() - leakTimeStamp) / 1000)) * leakRate;
        water.set(Math.max(0, waterLeft));
        // 重新更新leakTimeStamp
        leakTimeStamp = System.currentTimeMillis();
        // 尝试加水,并且水还未满
        if ((water.get()) < capacity) {
            water.addAndGet(1);
            return true;
        } else {
            // 水满,拒绝加水
            return false;
        }
    }
}
