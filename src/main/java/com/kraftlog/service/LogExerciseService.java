package com.kraftlog.service;

import com.kraftlog.config.CacheConfig;
import com.kraftlog.dto.LogExerciseCreateRequest;
import com.kraftlog.dto.LogExerciseResponse;
import com.kraftlog.entity.Exercise;
import com.kraftlog.entity.LogExercise;
import com.kraftlog.entity.LogWorkout;
import com.kraftlog.exception.ResourceNotFoundException;
import com.kraftlog.repository.ExerciseRepository;
import com.kraftlog.repository.LogExerciseRepository;
import com.kraftlog.repository.LogWorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LogExerciseService {

    private final LogExerciseRepository logExerciseRepository;
    private final LogWorkoutRepository logWorkoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final ModelMapper modelMapper;

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.LOG_EXERCISES_CACHE, allEntries = true)
    })
    public LogExerciseResponse createLogExercise(LogExerciseCreateRequest request) {
        LogWorkout logWorkout = logWorkoutRepository.findById(request.getLogWorkoutId())
                .orElseThrow(() -> new ResourceNotFoundException("LogWorkout", "id", request.getLogWorkoutId()));

        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", request.getExerciseId()));

        LogExercise logExercise = LogExercise.builder()
                .logWorkout(logWorkout)
                .exercise(exercise)
                .startDatetime(request.getStartDatetime())
                .endDatetime(request.getEndDatetime())
                .notes(request.getNotes())
                .repetitions(request.getRepetitions())
                .completed(request.getCompleted() != null ? request.getCompleted() : false)
                .build();

        LogExercise savedLogExercise = logExerciseRepository.save(logExercise);
        return mapToResponse(savedLogExercise);
    }

    @Cacheable(value = CacheConfig.LOG_EXERCISE_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public LogExerciseResponse getLogExerciseById(UUID id) {
        LogExercise logExercise = logExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LogExercise", "id", id));
        return mapToResponse(logExercise);
    }

    @Cacheable(value = CacheConfig.LOG_EXERCISES_CACHE)
    @Transactional(readOnly = true)
    public List<LogExerciseResponse> getAllLogExercises() {
        return logExerciseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.LOG_EXERCISES_CACHE, key = "'logWorkout-' + #logWorkoutId")
    @Transactional(readOnly = true)
    public List<LogExerciseResponse> getLogExercisesByLogWorkoutId(UUID logWorkoutId) {
        if (!logWorkoutRepository.existsById(logWorkoutId)) {
            throw new ResourceNotFoundException("LogWorkout", "id", logWorkoutId);
        }
        return logExerciseRepository.findByLogWorkoutId(logWorkoutId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.LOG_EXERCISE_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.LOG_EXERCISES_CACHE, allEntries = true)
    })
    public LogExerciseResponse updateLogExercise(UUID id, LogExerciseCreateRequest request) {
        LogExercise logExercise = logExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LogExercise", "id", id));

        if (request.getStartDatetime() != null) {
            logExercise.setStartDatetime(request.getStartDatetime());
        }
        if (request.getEndDatetime() != null) {
            logExercise.setEndDatetime(request.getEndDatetime());
        }
        if (request.getNotes() != null) {
            logExercise.setNotes(request.getNotes());
        }
        if (request.getRepetitions() != null) {
            logExercise.setRepetitions(request.getRepetitions());
        }
        if (request.getCompleted() != null) {
            logExercise.setCompleted(request.getCompleted());
        }

        LogExercise updatedLogExercise = logExerciseRepository.save(logExercise);
        return mapToResponse(updatedLogExercise);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.LOG_EXERCISE_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.LOG_EXERCISES_CACHE, allEntries = true)
    })
    public void deleteLogExercise(UUID id) {
        LogExercise logExercise = logExerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LogExercise", "id", id));
        logExerciseRepository.delete(logExercise);
    }

    private LogExerciseResponse mapToResponse(LogExercise logExercise) {
        LogExerciseResponse response = modelMapper.map(logExercise, LogExerciseResponse.class);
        response.setLogWorkoutId(logExercise.getLogWorkout().getId());
        response.setExerciseId(logExercise.getExercise().getId());
        response.setExerciseName(logExercise.getExercise().getName());
        return response;
    }
}
