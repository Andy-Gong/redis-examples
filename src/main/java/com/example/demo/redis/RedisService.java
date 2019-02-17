package com.example.demo.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
public class RedisService {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * Try to get lock, if the lock doesn't exist yet, create the lock, return true,
     * if other thread has hold the lock, returns false.
     *
     * @param lockName
     * @param value
     * @param expireTime
     * @return
     */
    public boolean getLock(String lockName, String value, int expireTime) {
        return redisTemplate.opsForValue().setIfAbsent(lockName, value, expireTime, TimeUnit.SECONDS);
    }

    /**
     * release lock, if current thread hold the lock, return true,
     * if current thread doesn't hold the lock, return false.
     *
     * @param lockName
     * @param value
     * @return
     */
    public boolean releaseLock(String lockName, String value) {
        Long result = (Long) redisTemplate.execute(RedisScript.of("if redis.call('get', KEYS[1]) == ARGV[1] " +
                        "then " +
                        "return redis.call('del', KEYS[1]) " +
                        "else return 0 " +
                        "end",
                Long.class),
                Collections.singletonList(lockName), value);
        return result == 1l ? true : false;
    }
}
