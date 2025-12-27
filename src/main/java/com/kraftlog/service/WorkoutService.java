package com.kraftlog.service;

import com.kraftlog.config.CacheConfig;
import com.kraftlog.dto.WorkoutCreateRequest;
import com.kraftlog.dto.WorkoutExerciseRequest;
import com.kraftlog.dto.WorkoutExerciseResponse;
import com.kraftlog.dto.WorkoutResponse;
import com.kraftlog.entity.Exercise;
import com.kraftlog.entity.Muscle;
import com.kraftlog.entity.Routine;
import com.kraftlog.entity.Workout;
import com.kraftlog.entity.WorkoutExercise;
import com.kraftlog.exception.ResourceNotFoundException;
import com.kraftlog.repository.ExerciseRepository;
import com.kraftlog.repository.MuscleRepository;
import com.kraftlog.repository.RoutineRepository;
import com.kraftlog.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final RoutineRepository routineRepository;
    private final ExerciseRepository exerciseRepository;
    private final MuscleRepository muscleRepository;
    private final ModelMapper modelMapper;

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.WORKOUTS_CACHE, allEntries = true)
    })
    public WorkoutResponse createWorkout(WorkoutCreateRequest request) {
        Routine routine = routineRepository.findById(request.getRoutineId())
                .orElseThrow(() -> new ResourceNotFoundException("Routine", "id", request.getRoutineId()));

        List<Muscle> muscles = new ArrayList<>();
        if (request.getMuscleIds() != null && !request.getMuscleIds().isEmpty()) {
            muscles = muscleRepository.findAllById(request.getMuscleIds());
        }

        Workout workout = Workout.builder()
                .name(request.getName())
                .orderIndex(request.getOrderIndex())
                .intervalMinutes(request.getIntervalMinutes())
                .routine(routine)
                .muscles(muscles)
                .build();

        Workout savedWorkout = workoutRepository.save(workout);
        
        // Now add workout exercises with additional details
        if (request.getExercises() != null && !request.getExercises().isEmpty()) {
            List<WorkoutExercise> workoutExercises = new ArrayList<>();
            for (WorkoutExerciseRequest exerciseRequest : request.getExercises()) {
                Exercise exercise = exerciseRepository.findById(exerciseRequest.getExerciseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", exerciseRequest.getExerciseId()));
                
                WorkoutExercise workoutExercise = WorkoutExercise.builder()
                        .workoutId(savedWorkout.getId())
                        .exerciseId(exercise.getId())
                        .workout(savedWorkout)
                        .exercise(exercise)
                        .recommendedSets(exerciseRequest.getRecommendedSets())
                        .recommendedReps(exerciseRequest.getRecommendedReps())
                        .trainingTechnique(exerciseRequest.getTrainingTechnique())
                        .orderIndex(exerciseRequest.getOrderIndex())
                        .build();
                workoutExercises.add(workoutExercise);
            }
            savedWorkout.setWorkoutExercises(workoutExercises);
            savedWorkout = workoutRepository.save(savedWorkout);
        }
        
        return mapWorkoutToResponse(savedWorkout);
    }

    @Cacheable(value = CacheConfig.WORKOUT_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public WorkoutResponse getWorkoutById(UUID id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout", "id", id));
        return mapWorkoutToResponse(workout);
    }

    @Cacheable(value = CacheConfig.WORKOUTS_CACHE)
    @Transactional(readOnly = true)
    public List<WorkoutResponse> getAllWorkouts() {
        return workoutRepository.findAll().stream()
                .map(this::mapWorkoutToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CacheConfig.WORKOUTS_CACHE, key = "'routine-' + #routineId")
    @Transactional(readOnly = true)
    public List<WorkoutResponse> getWorkoutsByRoutineId(UUID routineId) {
        if (!routineRepository.existsById(routineId)) {
            throw new ResourceNotFoundException("Routine", "id", routineId);
        }
        return workoutRepository.findByRoutineIdOrderByOrderIndexAsc(routineId).stream()
                .map(this::mapWorkoutToResponse)
                .collect(Collectors.toList());
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.WORKOUT_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.WORKOUTS_CACHE, allEntries = true)
    })
    public WorkoutResponse updateWorkout(UUID id, WorkoutCreateRequest request) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout", "id", id));

        if (request.getName() != null) {
            workout.setName(request.getName());
        }
        if (request.getOrderIndex() != null) {
            workout.setOrderIndex(request.getOrderIndex());
        }
        if (request.getIntervalMinutes() != null) {
            workout.setIntervalMinutes(request.getIntervalMinutes());
        }

        if (request.getExercises() != null) {
            // Clear existing workout exercises
            workout.getWorkoutExercises().clear();
            
            // Add new workout exercises
            List<WorkoutExercise> workoutExercises = new ArrayList<>();
            for (WorkoutExerciseRequest exerciseRequest : request.getExercises()) {
                Exercise exercise = exerciseRepository.findById(exerciseRequest.getExerciseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", exerciseRequest.getExerciseId()));
                
                WorkoutExercise workoutExercise = WorkoutExercise.builder()
                        .workoutId(workout.getId())
                        .exerciseId(exercise.getId())
                        .workout(workout)
                        .exercise(exercise)
                        .recommendedSets(exerciseRequest.getRecommendedSets())
                        .recommendedReps(exerciseRequest.getRecommendedReps())
                        .trainingTechnique(exerciseRequest.getTrainingTechnique())
                        .orderIndex(exerciseRequest.getOrderIndex())
                        .build();
                workoutExercises.add(workoutExercise);
            }
            workout.setWorkoutExercises(workoutExercises);
        }

        if (request.getMuscleIds() != null) {
            List<Muscle> muscles = muscleRepository.findAllById(request.getMuscleIds());
            workout.setMuscles(muscles);
        }

        Workout updatedWorkout = workoutRepository.save(workout);
        return mapWorkoutToResponse(updatedWorkout);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.WORKOUT_CACHE, key = "#id"),
            @CacheEvict(value = CacheConfig.WORKOUTS_CACHE, allEntries = true)
    })
    public void deleteWorkout(UUID id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout", "id", id));
        workoutRepository.delete(workout);
    }

    public WorkoutResponse mapWorkoutToResponse(Workout workout) {
        WorkoutResponse response = new WorkoutResponse();
        response.setId(workout.getId());
        response.setName(workout.getName());
        response.setOrderIndex(workout.getOrderIndex());
        response.setIntervalMinutes(workout.getIntervalMinutes());
        response.setRoutineId(workout.getRoutine().getId());
        response.setCreatedAt(workout.getCreatedAt());
        response.setUpdatedAt(workout.getUpdatedAt());
        
        // Map workout exercises
        if (workout.getWorkoutExercises() != null) {
            List<WorkoutExerciseResponse> exerciseResponses = workout.getWorkoutExercises().stream()
                    .map(we -> WorkoutExerciseResponse.builder()
                            .exerciseId(we.getExerciseId())
                            .exerciseName(we.getExercise().getName())
                            .exerciseDescription(we.getExercise().getDescription())
                            .videoUrl(we.getExercise().getVideoUrl())
                            .recommendedSets(we.getRecommendedSets())
                            .recommendedReps(we.getRecommendedReps())
                            .trainingTechnique(we.getTrainingTechnique())
                            .orderIndex(we.getOrderIndex())
                            .build())
                    .collect(Collectors.toList());
            response.setExercises(exerciseResponses);
        }
        
        // Map muscles
        if (workout.getMuscles() != null) {
            response.setMuscles(workout.getMuscles().stream()
                    .map(muscle -> modelMapper.map(muscle, com.kraftlog.dto.MuscleResponse.class))
                    .collect(Collectors.toList()));
        }
        
        return response;
    }
}
