package com.kraftlog.controller;

import com.kraftlog.dto.LogRoutineCreateRequest;
import com.kraftlog.dto.LogRoutineResponse;
import com.kraftlog.service.LogRoutineService;
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
@RequestMapping("/api/log-routines")
@RequiredArgsConstructor
@Tag(name = "Routine Logging", description = "APIs for logging workout routine sessions")
@SecurityRequirement(name = "bearer-jwt")
public class LogRoutineController {

    private final LogRoutineService logRoutineService;

    @Operation(summary = "Start a routine session", description = "Creates a new log entry for starting a routine workout session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Routine session started successfully",
                    content = @Content(schema = @Schema(implementation = LogRoutineResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Routine not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<LogRoutineResponse> startRoutineSession(@Valid @RequestBody LogRoutineCreateRequest request) {
        LogRoutineResponse response = logRoutineService.createLogRoutine(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get routine session by ID", description = "Returns a logged routine session by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine session found",
                    content = @Content(schema = @Schema(implementation = LogRoutineResponse.class))),
            @ApiResponse(responseCode = "404", description = "Routine session not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<LogRoutineResponse> getLogRoutineById(
            @Parameter(description = "Log Routine ID") @PathVariable UUID id) {
        LogRoutineResponse response = logRoutineService.getLogRoutineById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all routine sessions", description = "Returns all logged routine sessions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine sessions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LogRoutineResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<LogRoutineResponse>> getAllLogRoutines() {
        List<LogRoutineResponse> logRoutines = logRoutineService.getAllLogRoutines();
        return ResponseEntity.ok(logRoutines);
    }

    @Operation(summary = "Get routine sessions by user ID", description = "Returns all logged routine sessions for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine sessions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LogRoutineResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LogRoutineResponse>> getLogRoutinesByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<LogRoutineResponse> logRoutines = logRoutineService.getLogRoutinesByUserId(userId);
        return ResponseEntity.ok(logRoutines);
    }

    @Operation(summary = "Get routine sessions by routine ID", description = "Returns all logged routine sessions for a specific routine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine sessions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LogRoutineResponse.class))),
            @ApiResponse(responseCode = "404", description = "Routine not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/routine/{routineId}")
    public ResponseEntity<List<LogRoutineResponse>> getLogRoutinesByRoutineId(
            @Parameter(description = "Routine ID") @PathVariable UUID routineId) {
        List<LogRoutineResponse> logRoutines = logRoutineService.getLogRoutinesByRoutineId(routineId);
        return ResponseEntity.ok(logRoutines);
    }

    @Operation(summary = "Update routine session", description = "Updates a logged routine session (typically to set end time)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine session updated successfully",
                    content = @Content(schema = @Schema(implementation = LogRoutineResponse.class))),
            @ApiResponse(responseCode = "404", description = "Routine session not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<LogRoutineResponse> updateLogRoutine(
            @Parameter(description = "Log Routine ID") @PathVariable UUID id,
            @Valid @RequestBody LogRoutineCreateRequest request) {
        LogRoutineResponse response = logRoutineService.updateLogRoutine(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete routine session", description = "Deletes a logged routine session by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Routine session deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Routine session not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLogRoutine(
            @Parameter(description = "Log Routine ID") @PathVariable UUID id) {
        logRoutineService.deleteLogRoutine(id);
        return ResponseEntity.noContent().build();
    }
}
