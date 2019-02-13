package com.revolut.backend.utils;

import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtils {
    private static final int MAX_TIME_IN_MS_TO_INIT_THREAD = 10;
    private static final int MAX_TIME_IN_MS_TO_EXEC_OPERATION = 1000;

    public static void runConcurrently(Collection<Runnable> operations) {
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
        runConcurrently(operations, exceptions);
        assertTrue(exceptions.isEmpty());
    }

    public static void runConcurrently(Collection<Runnable> operations, List<Throwable> exceptions) {
        try {
            ExecutorService threadPool = Executors.newFixedThreadPool(operations.size());
            try {
                doRunConcurrently(operations, exceptions, threadPool);
            } finally {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void doRunConcurrently(Collection<Runnable> runnables, List<Throwable> exceptions, ExecutorService threadPool) throws InterruptedException {
        int numConcurrent = runnables.size();
        CountDownLatch untilAllSetUp = new CountDownLatch(numConcurrent);
        CountDownLatch untilStartAllowed = new CountDownLatch(1);
        CountDownLatch untilAllDone = new CountDownLatch(numConcurrent);
        for (Runnable runnable : runnables) {
            threadPool.submit(() -> {
                untilAllSetUp.countDown();
                try {
                    untilStartAllowed.await();
                    runnable.run();
                } catch (Throwable e) {
                    exceptions.add(e);
                } finally {
                    untilAllDone.countDown();
                }
            });
        }
        assertTrue(untilAllSetUp.await(MAX_TIME_IN_MS_TO_INIT_THREAD * numConcurrent, TimeUnit.MILLISECONDS));
        untilStartAllowed.countDown();
        assertTrue(untilAllDone.await(MAX_TIME_IN_MS_TO_EXEC_OPERATION * numConcurrent, TimeUnit.MILLISECONDS));
    }

    public static BigDecimal amount(double val) {
        return amount(val, false);
    }

    public static BigDecimal amount(double val, boolean roundToHundredths) {
        BigDecimal bd = new BigDecimal(Double.toString(val));
        return roundToHundredths ? bd.setScale(2, RoundingMode.HALF_UP) : bd;
    }

    public static void assertEquals(BigDecimal expected, BigDecimal actual) {
        Assertions.assertEquals(0, expected.compareTo(actual), String.format("Expected: %s\nActual: %s", expected.toString(), actual.toString()));
    }
}
