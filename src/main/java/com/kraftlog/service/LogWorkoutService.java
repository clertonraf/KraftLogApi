package com.kraftlog.service;

import com.kraftlog.config.CacheConfig;
import com.kraftlog.dto.LogWorkoutCreateRequest;
import com.kraftlog.dto.LogWorkoutResponse;
import com.kraftlog.entity.LogRoutine;
import com.kraftlog.entity.LogWorkout;
import com.kraftlog.entity.Workout;
import com.kraftlog.exception.ResourceNotFoundException;
import com.kraftlog.repository.LogRoutineRepository;
import com.kraftlog.repository.LogWorkoutRepository;
import com.kraftlog.repository.WorkoutRepository;
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
public class LogWorkoutService {

    private final LogWorkoutRepository logWorkoutRepository;
    private final LogRoutineRepository logRoutineRepository;
    private final WorkoutRepository workoutRepository;
    private final ModelMapper modelMapper;

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.LOG_WORKOUTS_CACHE, allEntries = true)
    })
    public LogWorkoutResponse createLogWorkout(LogWorkoutCreateRequest request) {
        LogRoutine logRoutine = logRoutineRepository.findById(request.getLogRoutineId())
                .orElseThrow(() -> new ResourceNotFoundException("LogRoutine", "id", request.getLogRoutineId()));

        Workout workout = workoutRepository.findById(request.getWorkoutId())
                .orElseThrow(() -> new ResourceNotFoundException("Workout", "id", request.getWorkoutId()));

        LogWorkout logWorkout = LogWorkout.builder()
                .logRoutine(logRoutine)
                .workout(workout)
                .startDatetime(request.getStartDatetime())
                .endDatetime(request.getEndDatetime())
                .build();

        LogWorkout savedLogWorkout = logWorkoutRepository.save(logWorkout);
        return mapToResponse(savedLogWorkout);
    }

    @Cacheable(value = CacheConfig.LOG_WORKOUT_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public LogWorkoutResponse getLogWorkoutById(UUID id) {
        LogWorkout logWorkout = logWorkoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LogWorkout", "id", id));
        return mapToResponse(logWorkout);
    }

    @Cacheable(value = CacheConfig.LOG_WORKOUTS_CACHE)
    @Transactional(readOnly = true)
    public List<LogWorkoutResponse> getAllLogWorkouts() {
        return logWorkoutRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.LOG_WORKOUTS_CACHE, key = "'logRoutine-' + #logRoutineId")
    @Transactional(readOnly = true)
    public List<LogWorkoutResponse> getLogWorkoutsByLogRoutineId(UUID logRoutineId) {
        if (!logRoutineRepository.existsById(logRoutineId)) {
            throw new ResourceNotFoundException("LogRoutine", "id", logRoutineId);
        }
        return logWorkoutRepository.findByLogRoutineId(logRoutineId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.LOG_WORKOUT_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.LOG_WORKOUTS_CACHE, allEntries = true)
    })
    public LogWorkoutResponse updateLogWorkout(UUID id, LogWorkoutCreateRequest request) {
        LogWorkout logWorkout = logWorkoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LogWorkout", "id", id));

        if (request.getStartDatetime() != null) {
            logWorkout.setStartDatetime(request.getStartDatetime());
        }
        if (request.getEndDatetime() != null) {
            logWorkout.setEndDatetime(request.getEndDatetime());
        }

        LogWorkout updatedLogWorkout = logWorkoutRepository.save(logWorkout);
        return mapToResponse(updatedLogWorkout);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.LOG_WORKOUT_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.LOG_WORKOUTS_CACHE, allEntries = true)
    })
    public void deleteLogWorkout(UUID id) {
        LogWorkout logWorkout = logWorkoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LogWorkout", "id", id));
        logWorkoutRepository.delete(logWorkout);
    }

    private LogWorkoutResponse mapToResponse(LogWorkout logWorkout) {
        LogWorkoutResponse response = modelMapper.map(logWorkout, LogWorkoutResponse.class);
        response.setLogRoutineId(logWorkout.getLogRoutine().getId());
        response.setWorkoutId(logWorkout.getWorkout().getId());
        return response;
    }
}
