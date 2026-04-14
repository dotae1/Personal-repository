package com.example.playlist.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    /**
     * Lettuce: Java에서 Redis랑 통신하기 위한 클라이언트 라이브러리
     * RedisConnectionFactory : Spring Container에 빈 등록
     * RedisStandaloneConfiguration : 단일 Redis 서버 연결 설정
     * config.setHostName/port/password(설정)
     * return new LettuceConnectionFactory : Lettuce 기반 Redis 클라이언트 사용하여 Spring이 이걸 통해 Redis랑 통신 진행함
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setPassword(password);

        return new LettuceConnectionFactory(config);
    }
}
