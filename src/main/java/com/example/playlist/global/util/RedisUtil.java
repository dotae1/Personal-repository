package com.example.playlist.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
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
    }

    public Long getExpire(String key, TimeUnit timeUnit) {
        return stringRedisTemplate.getExpire(key, timeUnit);
    }
}

