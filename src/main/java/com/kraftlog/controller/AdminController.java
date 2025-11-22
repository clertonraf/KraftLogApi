package com.kraftlog.controller;

import com.kraftlog.dto.ChangePasswordRequest;
import com.kraftlog.dto.UserResponse;
import com.kraftlog.service.AdminService;
import com.kraftlog.util.DatabaseCleaner;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Administrative APIs for user management (Admin only)")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final DatabaseCleaner databaseCleaner;

    @Operation(summary = "Delete user (Admin only)",
               description = "Deletes a user and all associated data. Only administrators can delete users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content)
    })
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "UUID of the user to delete", required = true)
            @PathVariable UUID userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change user password (Admin only)",
               description = "Changes the password for any user. Only administrators can change other users' passwords.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content)
    })
    @PutMapping("/users/{userId}/password")
    public ResponseEntity<UserResponse> changeUserPassword(
            @Parameter(description = "UUID of the user", required = true)
            @PathVariable UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        UserResponse response = adminService.changeUserPassword(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Clean database (truncate all tables)",
            description = "⚠️ WARNING: Truncates all tables, deleting ALL data while maintaining schema. This action cannot be undone!"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Database cleaned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to clean database", content = @Content)
    })
    @PostMapping("/database/clean")
    public ResponseEntity<Map<String, String>> cleanDatabase() {
        log.warn("⚠️ Admin requested database cleanup");
        
        try {
            databaseCleaner.truncateAllTables();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Database cleaned successfully. All data has been deleted.",
                    "note", "Restart the application to recreate the admin user."
            ));
        } catch (Exception e) {
            log.error("Failed to clean database", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to clean database: " + e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Delete all entities using JPA",
            description = "⚠️ WARNING: Deletes all data using JPA EntityManager (slower but safer). This action cannot be undone!"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All entities deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to delete entities", content = @Content)
    })
    @PostMapping("/database/delete-entities")
    public ResponseEntity<Map<String, String>> deleteAllEntities() {
        log.warn("⚠️ Admin requested entity deletion");
        
        try {
            databaseCleaner.deleteAllEntities();
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "All entities deleted successfully.",
                    "note", "Restart the application to recreate the admin user."
            ));
        } catch (Exception e) {
            log.error("Failed to delete entities", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to delete entities: " + e.getMessage()
            ));
        }
    }
}