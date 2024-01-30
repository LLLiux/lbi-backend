package com.lin.lbi.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedissonManagerTest {

    @Resource
    private RedissonManager redissonManager;

    @Test
    void doRateLimit() {
        for (int i = 0; i < 20; i++) {
            System.out.println(i);
            redissonManager.doRateLimit(String.valueOf(i%2));
        }
    }
}