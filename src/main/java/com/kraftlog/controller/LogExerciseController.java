package com.kraftlog.controller;

import com.kraftlog.dto.LogExerciseCreateRequest;
import com.kraftlog.dto.LogExerciseResponse;
import com.kraftlog.service.LogExerciseService;
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
@RequestMapping("/api/log-exercises")
@RequiredArgsConstructor
@Tag(name = "Exercise Logging", description = "APIs for logging individual exercises during workouts")
@SecurityRequirement(name = "bearer-jwt")
public class LogExerciseController {

    private final LogExerciseService logExerciseService;

    @Operation(summary = "Log an exercise", description = "Creates a new log entry for an exercise during a workout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exercise logged successfully",
                    content = @Content(schema = @Schema(implementation = LogExerciseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "LogWorkout or Exercise not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<LogExerciseResponse> logExercise(@Valid @RequestBody LogExerciseCreateRequest request) {
        LogExerciseResponse response = logExerciseService.createLogExercise(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get logged exercise by ID", description = "Returns a logged exercise by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercise found",
                    content = @Content(schema = @Schema(implementation = LogExerciseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<LogExerciseResponse> getLogExerciseById(
            @Parameter(description = "Log Exercise ID") @PathVariable UUID id) {
        LogExerciseResponse response = logExerciseService.getLogExerciseById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all logged exercises", description = "Returns all logged exercises")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LogExerciseResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<LogExerciseResponse>> getAllLogExercises() {
        List<LogExerciseResponse> logExercises = logExerciseService.getAllLogExercises();
        return ResponseEntity.ok(logExercises);
    }

    @Operation(summary = "Get exercises by workout", description = "Returns all logged exercises for a specific workout session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LogExerciseResponse.class))),
            @ApiResponse(responseCode = "404", description = "LogWorkout not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/log-workout/{logWorkoutId}")
    public ResponseEntity<List<LogExerciseResponse>> getLogExercisesByLogWorkoutId(
            @Parameter(description = "Log Workout ID") @PathVariable UUID logWorkoutId) {
        List<LogExerciseResponse> logExercises = logExerciseService.getLogExercisesByLogWorkoutId(logWorkoutId);
        return ResponseEntity.ok(logExercises);
    }

    @Operation(summary = "Update logged exercise", description = "Updates a logged exercise (for corrections)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exercise updated successfully",
                    content = @Content(schema = @Schema(implementation = LogExerciseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<LogExerciseResponse> updateLogExercise(
            @Parameter(description = "Log Exercise ID") @PathVariable UUID id,
            @Valid @RequestBody LogExerciseCreateRequest request) {
        LogExerciseResponse response = logExerciseService.updateLogExercise(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete logged exercise", description = "Deletes a logged exercise by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Exercise deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLogExercise(
            @Parameter(description = "Log Exercise ID") @PathVariable UUID id) {
        logExerciseService.deleteLogExercise(id);
        return ResponseEntity.noContent().build();
    }
}
