package com.example.playlist.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    public void setData(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public String getData(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void deleteData(String key) {
        stringRedisTemplate.delete(key);
    }

    public void setDataExpire(String key, String value, long time) {
        stringRedisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        log.info("Set key={} value={} expire={}s", key, value, time);
    }

    public Long getExpire(String key, TimeUnit timeUnit) {
        return stringRedisTemplate.getExpire(key, timeUnit);
    }
}

