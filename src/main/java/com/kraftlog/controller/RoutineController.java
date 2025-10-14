package com.kraftlog.controller;

import com.kraftlog.dto.RoutineCreateRequest;
import com.kraftlog.dto.RoutineResponse;
import com.kraftlog.service.RoutineService;
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
@RequestMapping("/api/routines")
@RequiredArgsConstructor
@Tag(name = "Routine Management", description = "APIs for managing workout routines")
@SecurityRequirement(name = "bearer-jwt")
public class RoutineController {

    private final RoutineService routineService;

    @Operation(summary = "Create a new routine", description = "Creates a new workout routine for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Routine created successfully",
                    content = @Content(schema = @Schema(implementation = RoutineResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<RoutineResponse> createRoutine(@Valid @RequestBody RoutineCreateRequest request) {
        RoutineResponse response = routineService.createRoutine(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get routine by ID", description = "Returns a single routine by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine found",
                    content = @Content(schema = @Schema(implementation = RoutineResponse.class))),
            @ApiResponse(responseCode = "404", description = "Routine not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<RoutineResponse> getRoutineById(
            @Parameter(description = "Routine ID") @PathVariable UUID id) {
        RoutineResponse response = routineService.getRoutineById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all routines", description = "Returns all routines")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routines retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RoutineResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<RoutineResponse>> getAllRoutines() {
        List<RoutineResponse> routines = routineService.getAllRoutines();
        return ResponseEntity.ok(routines);
    }

    @Operation(summary = "Get routines by user ID", description = "Returns all routines for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routines retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RoutineResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RoutineResponse>> getRoutinesByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<RoutineResponse> routines = routineService.getRoutinesByUserId(userId);
        return ResponseEntity.ok(routines);
    }

    @Operation(summary = "Update routine", description = "Updates an existing routine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routine updated successfully",
                    content = @Content(schema = @Schema(implementation = RoutineResponse.class))),
            @ApiResponse(responseCode = "404", description = "Routine not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<RoutineResponse> updateRoutine(
            @Parameter(description = "Routine ID") @PathVariable UUID id,
            @Valid @RequestBody RoutineCreateRequest request) {
        RoutineResponse response = routineService.updateRoutine(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete routine", description = "Deletes a routine by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Routine deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Routine not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoutine(
            @Parameter(description = "Routine ID") @PathVariable UUID id) {
        routineService.deleteRoutine(id);
        return ResponseEntity.noContent().build();
    }
}