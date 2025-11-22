package com.kraftlog.controller;

import com.kraftlog.dto.WorkoutCreateRequest;
import com.kraftlog.dto.WorkoutResponse;
import com.kraftlog.service.WorkoutService;
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
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@Tag(name = "Workout Management", description = "APIs for managing workouts within routines")
@SecurityRequirement(name = "bearer-jwt")
public class WorkoutController {

    private final WorkoutService workoutService;

    @Operation(summary = "Create a new workout", description = "Creates a new workout for a routine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Workout created successfully",
                    content = @Content(schema = @Schema(implementation = WorkoutResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Routine not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<WorkoutResponse> createWorkout(@Valid @RequestBody WorkoutCreateRequest request) {
        WorkoutResponse response = workoutService.createWorkout(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get workout by ID", description = "Returns a single workout by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workout found",
                    content = @Content(schema = @Schema(implementation = WorkoutResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workout not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponse> getWorkoutById(
            @Parameter(description = "Workout ID") @PathVariable UUID id) {
        WorkoutResponse response = workoutService.getWorkoutById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all workouts", description = "Returns all workouts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workouts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = WorkoutResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<WorkoutResponse>> getAllWorkouts() {
        List<WorkoutResponse> workouts = workoutService.getAllWorkouts();
        return ResponseEntity.ok(workouts);
    }

    @Operation(summary = "Get workouts by routine ID", description = "Returns all workouts for a specific routine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workouts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = WorkoutResponse.class))),
            @ApiResponse(responseCode = "404", description = "Routine not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/routine/{routineId}")
    public ResponseEntity<List<WorkoutResponse>> getWorkoutsByRoutineId(
            @Parameter(description = "Routine ID") @PathVariable UUID routineId) {
        List<WorkoutResponse> workouts = workoutService.getWorkoutsByRoutineId(routineId);
        return ResponseEntity.ok(workouts);
    }

    @Operation(summary = "Update workout", description = "Updates an existing workout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workout updated successfully",
                    content = @Content(schema = @Schema(implementation = WorkoutResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workout not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<WorkoutResponse> updateWorkout(
            @Parameter(description = "Workout ID") @PathVariable UUID id,
            @Valid @RequestBody WorkoutCreateRequest request) {
        WorkoutResponse response = workoutService.updateWorkout(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete workout", description = "Deletes a workout by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Workout deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Workout not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(
            @Parameter(description = "Workout ID") @PathVariable UUID id) {
        workoutService.deleteWorkout(id);
        return ResponseEntity.noContent().build();
    }
}
