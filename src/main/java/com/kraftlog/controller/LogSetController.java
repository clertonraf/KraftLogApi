package com.kraftlog.controller;

import com.kraftlog.dto.LogSetCreateRequest;
import com.kraftlog.dto.LogSetResponse;
import com.kraftlog.service.LogSetService;
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
@RequestMapping("/api/log-sets")
@RequiredArgsConstructor
@Tag(name = "Set Logging", description = "APIs for logging individual exercise sets during workouts")
@SecurityRequirement(name = "bearer-jwt")
public class LogSetController {

    private final LogSetService logSetService;

    @Operation(summary = "Log a set", description = "Creates a new log entry for a completed exercise set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Set logged successfully",
                    content = @Content(schema = @Schema(implementation = LogSetResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "LogExercise not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<LogSetResponse> logSet(@Valid @RequestBody LogSetCreateRequest request) {
        LogSetResponse response = logSetService.createLogSet(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get logged set by ID", description = "Returns a logged set by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Set found",
                    content = @Content(schema = @Schema(implementation = LogSetResponse.class))),
            @ApiResponse(responseCode = "404", description = "Set not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<LogSetResponse> getLogSetById(
            @Parameter(description = "Log Set ID") @PathVariable UUID id) {
        LogSetResponse response = logSetService.getLogSetById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all logged sets", description = "Returns all logged sets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sets retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LogSetResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<LogSetResponse>> getAllLogSets() {
        List<LogSetResponse> logSets = logSetService.getAllLogSets();
        return ResponseEntity.ok(logSets);
    }

    @Operation(summary = "Get sets by exercise", description = "Returns all logged sets for a specific logged exercise")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sets retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LogSetResponse.class))),
            @ApiResponse(responseCode = "404", description = "LogExercise not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/log-exercise/{logExerciseId}")
    public ResponseEntity<List<LogSetResponse>> getLogSetsByLogExerciseId(
            @Parameter(description = "Log Exercise ID") @PathVariable UUID logExerciseId) {
        List<LogSetResponse> logSets = logSetService.getLogSetsByLogExerciseId(logExerciseId);
        return ResponseEntity.ok(logSets);
    }

    @Operation(summary = "Update logged set", description = "Updates a logged set (for corrections)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Set updated successfully",
                    content = @Content(schema = @Schema(implementation = LogSetResponse.class))),
            @ApiResponse(responseCode = "404", description = "Set not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<LogSetResponse> updateLogSet(
            @Parameter(description = "Log Set ID") @PathVariable UUID id,
            @Valid @RequestBody LogSetCreateRequest request) {
        LogSetResponse response = logSetService.updateLogSet(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete logged set", description = "Deletes a logged set by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Set deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Set not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLogSet(
            @Parameter(description = "Log Set ID") @PathVariable UUID id) {
        logSetService.deleteLogSet(id);
        return ResponseEntity.noContent().build();
    }
}
