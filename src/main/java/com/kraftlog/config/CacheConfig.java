package com.kraftlog.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public static final String WORKOUTS_CACHE = "workouts";
    public static final String WORKOUT_CACHE = "workout";
    public static final String LOG_ROUTINES_CACHE = "logRoutines";
    public static final String LOG_ROUTINE_CACHE = "logRoutine";
    public static final String LOG_WORKOUTS_CACHE = "logWorkouts";
    public static final String LOG_WORKOUT_CACHE = "logWorkout";
    public static final String LOG_EXERCISES_CACHE = "logExercises";
    public static final String LOG_EXERCISE_CACHE = "logExercise";
    public static final String LOG_SETS_CACHE = "logSets";
    public static final String LOG_SET_CACHE = "logSet";

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
                ROUTINE_CACHE,
                WORKOUTS_CACHE,
                WORKOUT_CACHE,
                LOG_ROUTINES_CACHE,
                LOG_ROUTINE_CACHE,
                LOG_WORKOUTS_CACHE,
                LOG_WORKOUT_CACHE,
                LOG_EXERCISES_CACHE,
                LOG_EXERCISE_CACHE,
                LOG_SETS_CACHE,
                LOG_SET_CACHE
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .recordStats());

        return cacheManager;
    }
}