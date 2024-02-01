package com.lin.lbi.manager;

import com.lin.lbi.common.ErrorCode;
import com.lin.lbi.exception.ThrowUtils;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author L
 */
@Component
public class RedissonManager {

    @Resource
    private RedissonClient redissonClient;

    public boolean doRateLimit(String key) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(RateType.OVERALL, 1, 60, RateIntervalUnit.SECONDS);
        boolean canOp = rateLimiter.tryAcquire(1);
        return canOp;
    }
}
