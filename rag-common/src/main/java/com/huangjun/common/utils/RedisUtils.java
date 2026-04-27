package com.huangjun.common.utils;


import com.huangjun.common.domain.ChatMessage;
import com.huangjun.common.domain.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {
    private static RedisTemplate<String, FileInfo> redisTemplate ;
    private static StringRedisTemplate stringRedisTemplate ;
    private static RedisTemplate<String, ChatMessage> chatMessageDataMapper ;


    @Autowired
    public void setRedisTemplate(@Qualifier("redisFileInfoTemplate") RedisTemplate<String, FileInfo> redisTemplate,
                                 StringRedisTemplate stringRedisTemplate,
                                 @Qualifier("redisChatMessageTemplate") RedisTemplate<String, ChatMessage> chatMessageDataMapper) {
        RedisUtils.redisTemplate = redisTemplate;
        RedisUtils.stringRedisTemplate = stringRedisTemplate;
        RedisUtils.chatMessageDataMapper = chatMessageDataMapper;
    }

    public static void setString(String key, String value) {
        RedisUtils.stringRedisTemplate.opsForValue().set(key, value);
    }
    public static String getString(String key) {
        return RedisUtils.stringRedisTemplate.opsForValue().get(key);
    }
    public static void del(String key) {
        RedisUtils.stringRedisTemplate.delete(key);
    }
    public static void expire(String key, int seconds,TimeUnit timeUnit) {
        RedisUtils.stringRedisTemplate.expire(key, seconds, timeUnit);
    }
    public static void expire(String key, int seconds) {
        RedisUtils.stringRedisTemplate.expire(key, seconds, TimeUnit.HOURS);
    }

    public static void setHash(String key, String field , Object value) {
        RedisUtils.stringRedisTemplate.opsForHash().put(key, field, value);
    }
    public static void setFileInfoHash(String key, String field , FileInfo value) {
        RedisUtils.redisTemplate.opsForHash().put(key, field, value);
    }
    public static FileInfo getHash(String key,String field) {
        return (FileInfo) RedisUtils.redisTemplate.opsForHash().get(key, field);
    }
    public static void deleteHash(String key,String field) {
        RedisUtils.redisTemplate.opsForHash().delete(key, field);
    }
    public static void addValue(String key, String value){
        RedisUtils.stringRedisTemplate.opsForValue().set(key,value);
    }
    public static Set<String> getSetValues(String key){
        return RedisUtils.stringRedisTemplate.opsForSet().members(key);
    }

    public static List<String> range(String key){
        return RedisUtils.stringRedisTemplate.opsForList().range(key, Integer.MIN_VALUE, -1);
    }

    public static List<String> range(String key,Integer start, Integer end){
        return RedisUtils.stringRedisTemplate.opsForList().range(key, start, end);
    }

    public static void setStringList(String key, List<String> messageList) {
        for (String message : messageList) {
            RedisUtils.stringRedisTemplate.opsForList().rightPush(key, message);
        }
    }

    public static void chatMessagePush(String key,List<ChatMessage> chatMessageList){
        RedisUtils.chatMessageDataMapper.opsForList().rightPushAll(key, chatMessageList);
    }

    public static List<ChatMessage> chatMessageRange(String key, Integer start, Integer end){
        return RedisUtils.chatMessageDataMapper.opsForList().range(key, start, end);
    }

    public static List<ChatMessage> chatMessageRange(String key){
        return chatMessageRange(key,Integer.MIN_VALUE,-1);
    }
}
