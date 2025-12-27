package com.kraftlog.controller;

import com.kraftlog.dto.WorkoutExerciseRequest;
import com.kraftlog.dto.WorkoutExerciseResponse;
import com.kraftlog.service.WorkoutExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workout-exercises")
@RequiredArgsConstructor
@Tag(name = "Workout Exercise Management", description = "APIs for managing exercises in workouts")
@SecurityRequirement(name = "bearer-jwt")
public class WorkoutExerciseController {

    private final WorkoutExerciseService workoutExerciseService;

    @Operation(summary = "Add exercise to workout", description = "Adds an exercise to a workout with recommended sets, reps, and training technique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exercise added to workout successfully",
                    content = @Content(schema = @Schema(implementation = WorkoutExerciseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Workout or Exercise not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<WorkoutExerciseResponse> addExerciseToWorkout(
            @Valid @RequestBody WorkoutExerciseAddRequest request) {
        WorkoutExerciseResponse response = workoutExerciseService.addExerciseToWorkout(
                request.getWorkoutId(), 
                WorkoutExerciseRequest.builder()
                        .exerciseId(request.getExerciseId())
                        .recommendedSets(request.getRecommendedSets())
                        .recommendedReps(request.getRecommendedReps())
                        .trainingTechnique(request.getTrainingTechnique())
                        .orderIndex(request.getOrderIndex())
                        .build()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all exercises in a workout", description = "Returns all exercises configured for a specific workout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Workout not found", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<WorkoutExerciseResponse>> getWorkoutExercises(
            @Parameter(description = "Workout ID") @RequestParam UUID workoutId) {
        List<WorkoutExerciseResponse> exercises = workoutExerciseService.getWorkoutExercises(workoutId);
        return ResponseEntity.ok(exercises);
    }

    @Operation(summary = "Remove exercise from workout", description = "Removes an exercise from a workout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Exercise removed from workout successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Workout or Exercise not found", content = @Content)
    })
    @DeleteMapping
    public ResponseEntity<Void> removeExerciseFromWorkout(
            @Parameter(description = "Workout ID") @RequestParam UUID workoutId,
            @Parameter(description = "Exercise ID") @RequestParam UUID exerciseId) {
        workoutExerciseService.removeExerciseFromWorkout(workoutId, exerciseId);
        return ResponseEntity.noContent().build();
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class WorkoutExerciseAddRequest {
        @jakarta.validation.constraints.NotNull(message = "Workout ID is required")
        private UUID workoutId;
        
        @jakarta.validation.constraints.NotNull(message = "Exercise ID is required")
        private UUID exerciseId;
        
        @jakarta.validation.constraints.Positive(message = "Recommended sets must be positive")
        private Integer recommendedSets;
        
        @jakarta.validation.constraints.Positive(message = "Recommended reps must be positive")
        private Integer recommendedReps;
        
        private String trainingTechnique;
        
        private Integer orderIndex;
    }
}
