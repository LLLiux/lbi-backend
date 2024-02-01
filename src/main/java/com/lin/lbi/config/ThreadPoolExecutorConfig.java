package com.lin.lbi.config;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author L
 */
@Configuration
@Data
public class ThreadPoolExecutorConfig {

    private int corePoolSize = 2;

    private int maximumPoolSize = 4;

    private long keepAliveTime = 100;

    private TimeUnit unit = TimeUnit.SECONDS;

    private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(4);

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 1;
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("Thread" + count++);
                return thread;
            }
        };
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory
        );
        return threadPoolExecutor;
    }
}
