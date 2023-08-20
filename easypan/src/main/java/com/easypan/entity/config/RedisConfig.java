package com.easypan.entity.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig<V> {
    @Bean
    public RedisTemplate<String,V> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String, V> t = new RedisTemplate<>();
        t.setConnectionFactory(factory);
        //设置key的序列化方式
        t.setKeySerializer(RedisSerializer.string());
        //设置value的序列化方式
        t.setValueSerializer(RedisSerializer.json());
        //设置hash的key序列化方式
        t.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value序列化方式
        t.setHashValueSerializer(RedisSerializer.json());
        t.afterPropertiesSet();
        return t;
    }
}
