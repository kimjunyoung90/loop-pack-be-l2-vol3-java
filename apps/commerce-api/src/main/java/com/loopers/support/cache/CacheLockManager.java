package com.loopers.support.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Slf4j
@Component
public class CacheLockManager {

    private static final long LOCK_TIMEOUT_SECONDS = 3;
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public <T> T executeWithLock(String key, Supplier<Optional<T>> cacheCheck, Supplier<T> dbFallback) {
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());

        boolean acquired;
        try {
            acquired = lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock 획득 중 인터럽트 발생: key={}", key);
            return dbFallback.get();
        }

        if (!acquired) {
            log.warn("Lock 획득 타임아웃, DB 폴백: key={}", key);
            return dbFallback.get();
        }

        try {
            // Double-Check: 대기하는 동안 다른 스레드가 캐시를 채웠을 수 있음
            Optional<T> cached = cacheCheck.get();
            if (cached.isPresent()) {
                return cached.get();
            }
            return dbFallback.get();
        } finally {
            lock.unlock();
            locks.remove(key);
        }
    }
}
