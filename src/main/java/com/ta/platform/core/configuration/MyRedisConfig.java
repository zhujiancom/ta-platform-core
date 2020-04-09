package com.ta.platform.core.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.time.Duration;

@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableCaching
public class MyRedisConfig extends CachingConfigurerSupport {
    /**
     * 缓存配置管理器
     */
    @Bean
    public CacheManager cacheManager(ObjectProvider<LettuceConnectionFactory> factory) {
        // 以锁写入的方式创建RedisCacheWriter对象
        RedisCacheWriter writer = RedisCacheWriter.lockingRedisCacheWriter(factory.getObject());
        /**
         * 设置CacheManager的Value序列化方式为JdkSerializationRedisSerializer,
         * 但其实RedisCacheConfiguration默认就是使用 StringRedisSerializer序列化key，
         * JdkSerializationRedisSerializer序列化value, 所以以下注释代码就是默认实现，没必要写，直接注释掉
         */
        // RedisSerializationContext.SerializationPair pair =
        // RedisSerializationContext.SerializationPair.fromSerializer(new
        // JdkSerializationRedisSerializer(this.getClass().getClassLoader()));
        // RedisCacheConfiguration config =
        // RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(pair);
        // 创建默认缓存配置对象
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)); // 设置缓存有效期一小时;
        RedisCacheManager cacheManager = new RedisCacheManager(writer, config);
        return cacheManager;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(ObjectProvider<LettuceConnectionFactory> lettuceConnectionFactory) {
        // 设置序列化
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        // 配置redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory.getObject());
        RedisSerializer<?> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);// key序列化
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);// value序列化
        redisTemplate.setHashKeySerializer(stringSerializer);// Hash key序列化
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);// Hash value序列化
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public CookieSerializer cookieSerializer(){
        CookieSerializer cookieSerializer = new DefaultCookieSerializer();
        ((DefaultCookieSerializer) cookieSerializer).setCookiePath("/");
        return cookieSerializer;
    }


    /**
     * 如果你使用 @EnableRedisHttpSession , 那 SessionManageListener 和 Resis Keyspace event notifications功能默认是开启的
     * 但是在一些有安全机制的Redis环境中这个命令是不能用的. 你可以通过设置ConfigureRedisAction.NO_OP来关闭这个功能
     *
     * 典型场景: 在配置redis环境时, 使用redisTemplate连接时会报如下错误
     * org.springframework.data.redis.ClusterStateFailureException: Could not retrieve cluster information. CLUSTER NODES returned with error.
     * @return
     */
    @Bean
    public static ConfigureRedisAction configureRedisAction(){
        return ConfigureRedisAction.NO_OP;
    }

}
