package com.kraftlog.controller;

import com.kraftlog.service.ExerciseImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/exercises")
@RequiredArgsConstructor
@Tag(name = "Exercise Import", description = "APIs for importing exercises from PDF files (Admin only)")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
public class ExerciseImportController {

    private final ExerciseImportService exerciseImportService;

    @Operation(summary = "Import exercises from PDF file",
               description = "Upload a PDF file containing exercise data in Portuguese format. " +
                           "The PDF should have muscle group headers and exercise tables with video URLs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid PDF format or no exercises found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content)
    })
    @PostMapping("/import-pdf")
    public ResponseEntity<Map<String, Object>> importExercisesFromPdf(
            @RequestParam("file") MultipartFile file) {
        
        log.info("Received request to import exercises from PDF: {}", file.getOriginalFilename());
        
        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "File is empty"
            ));
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "File must be a PDF"
            ));
        }
        
        try {
            // Save uploaded file temporarily
            Path tempFile = Files.createTempFile("exercise-import-", ".pdf");
            file.transferTo(tempFile.toFile());
            
            try {
                // Import exercises
                ExerciseImportService.ImportResult result = 
                        exerciseImportService.importExercisesFromPdf(tempFile.toFile());
                
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Import completed",
                        "totalProcessed", result.getTotalCount(),
                        "successful", result.getSuccessCount(),
                        "failed", result.getFailureCount(),
                        "failures", result.getFailures()
                ));
            } finally {
                // Clean up temporary file
                Files.deleteIfExists(tempFile);
            }
            
        } catch (IOException e) {
            log.error("Failed to process PDF file", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to process PDF: " + e.getMessage()
            ));
        }
    }
}
