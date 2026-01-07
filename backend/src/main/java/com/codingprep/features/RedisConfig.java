package com.codingprep.features;

import com.codingprep.features.matchmaking.dto.LiveMatchStateDTO;
import com.codingprep.features.matchmaking.dto.PlayerMatchDTO;
import com.codingprep.features.matchmaking.models.PlayerDiscussionIdentification;
//import com.codingprep.features.match.service.MatchExpirationHandler;
import com.codingprep.features.problem.service.ProblemService;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.LongSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.jsonwebtoken.io.SerializationException;
import jakarta.persistence.criteria.CriteriaBuilder.In;
//import tools.jackson.databind.SerializationFeature;
//import tools.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
//import tools.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
//import org.springframework.data.redis.listener.PatternTopic;
//import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.Taskbar.Feature;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@Slf4j
@EnableRedisRepositories
public class RedisConfig {


		//RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
		//configuration.set
		//LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(();
		//connectionFactory.afterPropertiesSet();

		//RedisTemplate<String, String> template = new RedisTemplate<>();
		//template.setConnectionFactory(connectionFactory);

		//template.setDefaultSerializer(StringRedisSerializer.UTF_8);
		//template.afterPropertiesSet();

		//template.opsForValue().set("foo", "bar");


		//System.out.println("Value at foo:" + template.opsForValue().get("foo"));

		//connectionFactory.destroy();
    


      @Bean
    public RedisConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory("redis-livematch",6380);

      }
    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
       
        objectMapper.activateDefaultTyping(

                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL

                );
                         
        

        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(jsonRedisSerializer);

        //template.setHashValueSerializer(jsonRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    RedisTemplate<String, PlayerMatchDTO> redisPlayerMatchDTOTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, PlayerMatchDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
       
        objectMapper.activateDefaultTyping(

                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL

                );
                         
        

        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(jsonRedisSerializer);

        //template.setHashValueSerializer(jsonRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    RedisTemplate<String, PlayerDiscussionIdentification> redisPlayerDiscussionTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, PlayerDiscussionIdentification> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
       
        objectMapper.activateDefaultTyping(

                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL

                );
                         
        

        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(jsonRedisSerializer);

        //template.setHashValueSerializer(jsonRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }


    @Bean
    RedisTemplate<String, Long> redisStrToIntTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Long> template = new RedisTemplate<>();

        
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericToStringSerializer<>(Long.class));

        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();


        return template;
    }
}
