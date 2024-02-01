package com.lin.lbi.manager;

import com.lin.lbi.common.ErrorCode;
import com.lin.lbi.exception.ThrowUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedissonManagerTest {

    @Resource
    private RedissonManager redissonManager;

    @Test
    void doRateLimit() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            boolean canOp = redissonManager.doRateLimit("1");
            ThrowUtils.throwIf(!canOp, ErrorCode.TOO_MANY_REQUEST);
            System.out.println(i);
            Thread.sleep(5000);
        }
    }
}