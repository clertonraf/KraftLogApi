package com.kraftlog.service;

import com.kraftlog.config.CacheConfig;
import com.kraftlog.dto.RoutineCreateRequest;
import com.kraftlog.dto.RoutineResponse;
import com.kraftlog.dto.WorkoutResponse;
import com.kraftlog.entity.Routine;
import com.kraftlog.entity.User;
import com.kraftlog.exception.ResourceNotFoundException;
import com.kraftlog.repository.RoutineRepository;
import com.kraftlog.repository.UserRepository;
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
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final WorkoutService workoutService;

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ROUTINES_CACHE, allEntries = true)
    })
    public RoutineResponse createRoutine(RoutineCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Routine routine = Routine.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : false)
                .user(user)
                .build();

        Routine savedRoutine = routineRepository.save(routine);
        return mapToResponse(savedRoutine);
    }

    @Cacheable(value = CacheConfig.ROUTINE_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public RoutineResponse getRoutineById(UUID id) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routine", "id", id));
        return mapToResponse(routine);
    }

    @Cacheable(value = CacheConfig.ROUTINES_CACHE)
    @Transactional(readOnly = true)
    public List<RoutineResponse> getAllRoutines() {
        return routineRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.ROUTINES_CACHE, key = "'user-' + #userId")
    @Transactional(readOnly = true)
    public List<RoutineResponse> getRoutinesByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return routineRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ROUTINE_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.ROUTINES_CACHE, allEntries = true)
    })
    public RoutineResponse updateRoutine(UUID id, RoutineCreateRequest request) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routine", "id", id));

        if (request.getName() != null) {
            routine.setName(request.getName());
        }
        if (request.getStartDate() != null) {
            routine.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            routine.setEndDate(request.getEndDate());
        }
        if (request.getIsActive() != null) {
            routine.setIsActive(request.getIsActive());
        }

        Routine updatedRoutine = routineRepository.save(routine);
        return mapToResponse(updatedRoutine);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ROUTINE_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.ROUTINES_CACHE, allEntries = true)
    })
    public void deleteRoutine(UUID id) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routine", "id", id));
        routineRepository.delete(routine);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ROUTINE_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ROUTINES_CACHE, allEntries = true)
    })
    public RoutineResponse activateRoutine(UUID id) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routine", "id", id));
        
        // Deactivate all other routines for this user
        List<Routine> userRoutines = routineRepository.findByUserId(routine.getUser().getId());
        for (Routine r : userRoutines) {
            if (!r.getId().equals(id) && Boolean.TRUE.equals(r.getIsActive())) {
                r.setIsActive(false);
                routineRepository.save(r);
            }
        }
        
        // Activate this routine
        routine.setIsActive(true);
        Routine savedRoutine = routineRepository.save(routine);
        
        return mapToResponse(savedRoutine);
    }

    private RoutineResponse mapToResponse(Routine routine) {
        RoutineResponse response = modelMapper.map(routine, RoutineResponse.class);
        response.setUserId(routine.getUser().getId());
        
        // Properly map workouts with their exercises using WorkoutService
        if (routine.getWorkouts() != null) {
            List<WorkoutResponse> workoutResponses = routine.getWorkouts().stream()
                    .map(workout -> workoutService.mapWorkoutToResponse(workout))
                    .collect(Collectors.toList());
            response.setWorkouts(workoutResponses);
        }
        
        return response;
    }
}