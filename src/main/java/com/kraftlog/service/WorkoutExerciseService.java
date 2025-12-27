package com.kraftlog.service;

import com.kraftlog.dto.WorkoutExerciseRequest;
import com.kraftlog.dto.WorkoutExerciseResponse;
import com.kraftlog.entity.Exercise;
import com.kraftlog.entity.Workout;
import com.kraftlog.entity.WorkoutExercise;
import com.kraftlog.exception.ResourceNotFoundException;
import com.kraftlog.repository.ExerciseRepository;
import com.kraftlog.repository.WorkoutExerciseRepository;
import com.kraftlog.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutExerciseService {

    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;

    @Transactional
    public WorkoutExerciseResponse addExerciseToWorkout(UUID workoutId, WorkoutExerciseRequest request) {
        log.info("Adding exercise {} to workout {}", request.getExerciseId(), workoutId);
        
        // Verify workout exists
        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout", "id", workoutId));
        
        // Verify exercise exists
        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", request.getExerciseId()));
        
        // Create workout exercise
        WorkoutExercise workoutExercise = WorkoutExercise.builder()
                .workoutId(workoutId)
                .exerciseId(request.getExerciseId())
                .recommendedSets(request.getRecommendedSets())
                .recommendedReps(request.getRecommendedReps())
                .trainingTechnique(request.getTrainingTechnique())
                .orderIndex(request.getOrderIndex())
                .build();
        
        workoutExercise = workoutExerciseRepository.save(workoutExercise);
        log.info("Successfully added exercise to workout");
        
        return mapToResponse(workoutExercise, exercise);
    }

    @Transactional(readOnly = true)
    public List<WorkoutExerciseResponse> getWorkoutExercises(UUID workoutId) {
        log.info("Getting exercises for workout {}", workoutId);
        
        // Verify workout exists
        workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout", "id", workoutId));
        
        List<WorkoutExercise> workoutExercises = workoutExerciseRepository.findByWorkoutIdOrderByOrderIndexAsc(workoutId);
        
        return workoutExercises.stream()
                .map(we -> mapToResponse(we, we.getExercise()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeExerciseFromWorkout(UUID workoutId, UUID exerciseId) {
        log.info("Removing exercise {} from workout {}", exerciseId, workoutId);
        
        // Verify workout exists
        workoutRepository.findById(workoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout", "id", workoutId));
        
        // Verify exercise exists
        exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", exerciseId));
        
        workoutExerciseRepository.deleteByWorkoutIdAndExerciseId(workoutId, exerciseId);
        log.info("Successfully removed exercise from workout");
    }

    private WorkoutExerciseResponse mapToResponse(WorkoutExercise workoutExercise, Exercise exercise) {
        return WorkoutExerciseResponse.builder()
                .exerciseId(exercise.getId())
                .exerciseName(exercise.getName())
                .exerciseDescription(exercise.getDescription())
                .videoUrl(exercise.getVideoUrl())
                .recommendedSets(workoutExercise.getRecommendedSets())
                .recommendedReps(workoutExercise.getRecommendedReps())
                .trainingTechnique(workoutExercise.getTrainingTechnique())
                .orderIndex(workoutExercise.getOrderIndex())
                .build();
    }
}
