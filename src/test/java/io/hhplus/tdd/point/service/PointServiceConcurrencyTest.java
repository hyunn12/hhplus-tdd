package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.entity.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PointServiceConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("동시에 충전하는 경우")
    void chargePoint() throws InterruptedException {
        long userId = 1L;
        long initPoint = 1000L;

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        pointService.chargePoint(userId, initPoint);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    pointService.chargePoint(userId, 100L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        UserPoint result = pointService.getUserPoint(userId);
        assertEquals(initPoint + threadCount * 100L, result.point());
    }

    @Test
    @DisplayName("동시에 사용하는 경우 - 보유 포인트 내에서 사용")
    void usePoint() throws InterruptedException {
        long userId = 1L;
        long initPoint = 10000L;

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        pointService.chargePoint(userId, initPoint);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.usePoint(userId, 100L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        UserPoint result = pointService.getUserPoint(userId);
        assertEquals(initPoint - threadCount * 100, result.point());
    }

    @Test
    @DisplayName("동시에 사용하는 경우 - 보유 포인트 넘어서 사용")
    void useOverInitPoint() throws InterruptedException {
        long userId = 1L;
        long initPoint = 10000L;

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        pointService.chargePoint(userId, initPoint);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.usePoint(userId, 2000L);
                    successCount.incrementAndGet();
                } catch (RuntimeException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        assertAll(
                () -> assertEquals(successCount.get(), 5),
                () -> assertEquals(failCount.get(), 5)
        );
    }


    @Test
    @DisplayName("동시에 충전 및 사용하는 경우")
    public void testChargeAndUse() throws InterruptedException {
        long userId = 1L;
        long initPoint = 5000L;

        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        pointService.chargePoint(userId, initPoint);

        for (int i = 0; i < threadCount / 2; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargePoint(userId, 500);
                } finally {
                    latch.countDown();
                }
            });
            executorService.submit(() -> {
                try {
                    pointService.usePoint(userId, 500);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        UserPoint result = pointService.getUserPoint(userId);
        assertEquals(initPoint, result.point());
    }

}