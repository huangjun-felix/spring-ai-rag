package com.huangjun.common.config;

import com.huangjun.common.domain.ChatMessage;
import com.huangjun.common.domain.FileInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean("redisFileInfoTemplate")
    public RedisTemplate<String, FileInfo> setRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, FileInfo> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.java());
        redisTemplate.setHashKeySerializer(RedisSerializer.java());
        Jackson2JsonRedisSerializer<FileInfo> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(FileInfo.class);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean("redisChatMessageTemplate")
    public RedisTemplate<String, ChatMessage> setRedisChatMessageTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, ChatMessage> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.java());
        redisTemplate.setHashKeySerializer(RedisSerializer.java());
        Jackson2JsonRedisSerializer<ChatMessage> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(ChatMessage.class);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}
