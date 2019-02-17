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

Find RedisTest.class and run it. In the demo, it contains 10 threads to compete the LOCK. If one thread gets the LOCK, it will hold the LOCK 10 seconds, then release the LOCK, then other threads can compete the LOCK. If the LOCK get process is blocking until it gets the LOCK.
