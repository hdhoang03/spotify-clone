package com.spotify.spotify.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.redisson.Redisson;
//import org.redisson.api.RedissonClient;
//import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

//    @Value("${spring.data.redis.host:localhost}")
//    private String redisHost;
//
//    @Value("${spring.data.redis.port:6379}")
//    private int redisPort;

    private ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return mapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(buildObjectMapper());

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(buildObjectMapper());

        // Cấu hình mặc định cho tất cả các cache
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()                    // Không cache null → tránh cache empty result
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer));

        // Cấu hình TTL riêng cho từng cache
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        cacheConfigs.put("admin_dashboard", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("admin_dashboard_overview", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("admin_users_page", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("admin_songs_page", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("admin_artists_page", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("admin_albums_page", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("admin_categories_page", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("artists_page",    defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("artist_detail",   defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("albums_by_artist",defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("user_profile",    defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("my_info",         defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("user_playlists",  defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("my_playlists",    defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("playlist_detail", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("playlist_songs",  defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("like_status",     defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("premium_status",  defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("premium_details", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

//    @Bean
//    public RedisCacheConfiguration cacheConfiguration(){
//        return RedisCacheConfiguration.defaultCacheConfig()
//                .serializeValuesWith(
//                  RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
//    }

//    @Bean(destroyMethod = "shutdown")
//    public RedissonClient redissonClient(){
//        Config config = new Config();
//        config.useSingleServer()
//                .setAddress("redis://" + redisHost + ":" + redisPort);
//        return Redisson.create(config);
//    }
//
//    @Bean
//    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory){
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
//
//        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofMinutes(30)) //thời gian mặc định cache
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
//
//        return RedisCacheManager.builder(connectionFactory)
//                .cacheDefaults(config)
//                .build();
//    }
}
