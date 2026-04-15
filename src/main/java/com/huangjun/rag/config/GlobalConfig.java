package com.huangjun.rag.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPooled;

@Configuration
public class GlobalConfig {


    @Bean("redisVectorStore")
    public VectorStore redisVectorStore(OpenAiEmbeddingModel embeddingModel,
                                        RedisProperties redisProperties
                                        ) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMaxTotal(10);
        DefaultJedisClientConfig config = DefaultJedisClientConfig.builder()
                .password(redisProperties.getPassword())
                .timeoutMillis(5000)
                .build();
        HostAndPort hostAndPort = new  HostAndPort(redisProperties.getHost(), redisProperties.getPort());

        JedisPooled jedisPooled = new JedisPooled(hostAndPort,config);
        return RedisVectorStore.builder(jedisPooled,embeddingModel).build();
    }

}
