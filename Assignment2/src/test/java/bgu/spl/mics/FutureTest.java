package bgu.spl.mics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {

    private static Future<Integer> future;
    private static boolean flag;
    private static Integer result1;
    private static Integer result2;

    @Before
    public void setUp() throws Exception {
        future = new Future<Integer>();
        flag = false;
        result1 = null;
        result2 = null;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void get() {
        // gets the result.
        // if the result is null -> wait for the result and return it when available. (blocking)
        // otherwise -> return the result.
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000);
                future.resolve(6);
                flag = true;
            } catch (InterruptedException e) { }
        });
        thread.start();

        // attempt getting the result.
        Integer result = future.get();
        // main thread should be waiting for ~1000ms, check if the flag is set to true and the result is correct.
        assertTrue(flag && result == 6);
    }

    @Test
    public void resolve() {
        future.resolve(6);
        Integer result = future.get();
        assertTrue(result != null && result == 6);
    }

    @Test
    public void isDone() {
        assertFalse(future.isDone());
        future.resolve(6);
        assertTrue(future.isDone());
    }

    @Test
    public void testGet() {
        // 1. check when we didn't do resolve at all.
        // 2. check when we do resolve after timeout/2
        Thread thread1 = new Thread(() -> {
            result1 = future.get(1000, TimeUnit.MILLISECONDS);
        });
        Thread thread2 = new Thread(() -> {
            result2 = future.get(2000, TimeUnit.MILLISECONDS);
        });
        thread1.start();
        thread2.start();

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) { }
        future.resolve(6);

        assertNull(result1);
//        assertTrue(result2 != null && result2 == 6);
    }
}