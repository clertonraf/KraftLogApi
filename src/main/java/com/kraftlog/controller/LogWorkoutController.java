package com.kraftlog.controller;

import com.kraftlog.dto.LogWorkoutCreateRequest;
import com.kraftlog.dto.LogWorkoutResponse;
import com.kraftlog.service.LogWorkoutService;
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
@RequestMapping("/api/log-workouts")
@RequiredArgsConstructor
@Tag(name = "Workout Logging", description = "APIs for logging workout sessions")
@SecurityRequirement(name = "bearer-jwt")
public class LogWorkoutController {

    private final LogWorkoutService logWorkoutService;

    @Operation(summary = "Start a workout session", description = "Creates a new log entry for starting a workout session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Workout session started successfully",
                    content = @Content(schema = @Schema(implementation = LogWorkoutResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "LogRoutine or Workout not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<LogWorkoutResponse> startWorkoutSession(@Valid @RequestBody LogWorkoutCreateRequest request) {
        LogWorkoutResponse response = logWorkoutService.createLogWorkout(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get workout session by ID", description = "Returns a logged workout session by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workout session found",
                    content = @Content(schema = @Schema(implementation = LogWorkoutResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workout session not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<LogWorkoutResponse> getLogWorkoutById(
            @Parameter(description = "Log Workout ID") @PathVariable UUID id) {
        LogWorkoutResponse response = logWorkoutService.getLogWorkoutById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all workout sessions", description = "Returns all logged workout sessions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workout sessions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LogWorkoutResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<LogWorkoutResponse>> getAllLogWorkouts() {
        List<LogWorkoutResponse> logWorkouts = logWorkoutService.getAllLogWorkouts();
        return ResponseEntity.ok(logWorkouts);
    }

    @Operation(summary = "Get workout sessions by log routine ID", description = "Returns all logged workout sessions for a specific log routine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workout sessions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LogWorkoutResponse.class))),
            @ApiResponse(responseCode = "404", description = "LogRoutine not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/log-routine/{logRoutineId}")
    public ResponseEntity<List<LogWorkoutResponse>> getLogWorkoutsByLogRoutineId(
            @Parameter(description = "Log Routine ID") @PathVariable UUID logRoutineId) {
        List<LogWorkoutResponse> logWorkouts = logWorkoutService.getLogWorkoutsByLogRoutineId(logRoutineId);
        return ResponseEntity.ok(logWorkouts);
    }

    @Operation(summary = "Get last completed workout session", description = "Returns the last completed workout session for a specific workout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Last workout session retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LogWorkoutResponse.class))),
            @ApiResponse(responseCode = "404", description = "No completed workout found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/workout/{workoutId}/last")
    public ResponseEntity<LogWorkoutResponse> getLastCompletedWorkout(
            @Parameter(description = "Workout ID") @PathVariable UUID workoutId) {
        return logWorkoutService.getLastCompletedWorkout(workoutId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update workout session", description = "Updates a logged workout session (typically to set end time)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workout session updated successfully",
                    content = @Content(schema = @Schema(implementation = LogWorkoutResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workout session not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<LogWorkoutResponse> updateLogWorkout(
            @Parameter(description = "Log Workout ID") @PathVariable UUID id,
            @Valid @RequestBody LogWorkoutCreateRequest request) {
        LogWorkoutResponse response = logWorkoutService.updateLogWorkout(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete workout session", description = "Deletes a logged workout session by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Workout session deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Workout session not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLogWorkout(
            @Parameter(description = "Log Workout ID") @PathVariable UUID id) {
        logWorkoutService.deleteLogWorkout(id);
        return ResponseEntity.noContent().build();
    }
}
