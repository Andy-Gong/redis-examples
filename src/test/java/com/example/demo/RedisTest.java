package com.example.demo;

import com.example.demo.redis.RedisService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisService redisService;

    @Test
    public void testListAsQueue() {
        redisTemplate.opsForList().leftPush("test", "test");
        redisTemplate.opsForList().leftPush("test", "test1");
        redisTemplate.opsForList().leftPush("test", "test2");
        redisTemplate.opsForList().leftPush("test", "test3");
        redisTemplate.opsForList().leftPush("test", "test4");
        redisTemplate.opsForList().leftPush("test", "test5");
        Assert.assertEquals("test", redisTemplate.opsForList().rightPop("test"));
        Assert.assertEquals("test1", redisTemplate.opsForList().rightPop("test"));
        Assert.assertEquals("test2", redisTemplate.opsForList().rightPop("test"));
        Assert.assertEquals("test3", redisTemplate.opsForList().rightPop("test"));
        Assert.assertEquals("test4", redisTemplate.opsForList().rightPop("test"));
        Assert.assertEquals("test5", redisTemplate.opsForList().rightPop("test"));
    }

    @Test
    public void testDistributeLock() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        String lockName = "lockName";
        redisTemplate.delete(lockName);
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(executorService.submit(new GetLockJob(redisService, lockName)));
        }
        futures.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    class GetLockJob implements Callable {
        private final RedisService redisService;
        private final String lockName;

        public GetLockJob(RedisService redisService, String lockName) {
            this.redisService = redisService;
            this.lockName = lockName;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public Boolean call() throws Exception {
            boolean result = redisService.getLock(lockName, Thread.currentThread().getName(), 30);
            System.out.println(Thread.currentThread().getName() + "   " + result);
            if (result == true) {
                // current thread has got lock, and hold 10 seconds
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                }
            } else {
                // while() loop until get the lock
                tryToGetLock();
            }
            redisService.releaseLock(lockName, Thread.currentThread().getName());
            System.out.println(Thread.currentThread().getName() + " release lock");
            return true;
        }

        public void tryToGetLock() {
            boolean result = false;
            int i = 0;
            while (!result) {
                System.out.println(Thread.currentThread().getName() + " try to get the lock, times: " + i++);
                result = redisService.getLock(lockName, Thread.currentThread().getName(), 30);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            System.out.println(Thread.currentThread().getName() + " get lock");
        }
    }
}
