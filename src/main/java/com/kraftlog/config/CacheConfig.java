package com.kraftlog.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String EXERCISES_CACHE = "exercises";
    public static final String EXERCISE_CACHE = "exercise";
    public static final String MUSCLES_CACHE = "muscles";
    public static final String MUSCLE_CACHE = "muscle";
    public static final String USERS_CACHE = "users";
    public static final String USER_CACHE = "user";
    public static final String ROUTINES_CACHE = "routines";
    public static final String ROUTINE_CACHE = "routine";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                EXERCISES_CACHE,
                EXERCISE_CACHE,
                MUSCLES_CACHE,
                MUSCLE_CACHE,
                USERS_CACHE,
                USER_CACHE,
                ROUTINES_CACHE,
                ROUTINE_CACHE
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());

        return cacheManager;
    }
}