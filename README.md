# Redis-examples

This project is based on spring boot, it introduces how to build the distribute lock via Redis. Redis client adopts the spring-boot-starter-data-redis.
```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
```

# Percondition
- Maven 3++
- java 8


# Run application

1. Download source code

```
git clone https://github.com/Andy-Gong/redis-examples.git
```

2. Run with demo

   Find RedisTest.class and run it. In the demo, it contains 10 threads to compete the LOCK. If one thread gets the LOCK,        it will hold the LOCK 10 seconds, then release the LOCK, then other threads can compete the LOCK. If the LOCK get              process is blocking until it gets the LOCK.
   
3. How to get or release LOCK
   - Get LOCK
   If the lock doesn't exist, we will create a new LOCK with value and expireTime. 
   The value is used to during release the LOCK, ONLY the lock holder can release the LOCK, to void other thread, which can't    hold the lock currently, releases the lock.
   The expireTIme is used to void deadlock. The deadlock may happen when the lock holder down, but it can't release the LOCK      yet.
   ```
    public boolean getLock(String lockName, String value, int expireTime) {
        return redisTemplate.opsForValue().setIfAbsent(lockName, value, expireTime, TimeUnit.SECONDS);
    }
   ```
   - Release LOCK
   Release LOCK includes 2 steps:
     - check if current thread holds the lock, 
     - if not return false, if yes then delete the lock.
   To void concurrent issue, we holp the two steps should be atomic. To ensure the actomic, we adopt redis script (Lua) to        release LOCK.
   ```
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
   ```
