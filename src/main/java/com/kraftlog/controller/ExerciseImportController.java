package com.kraftlog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/exercises")
@RequiredArgsConstructor
@Tag(name = "Exercise Import", description = "APIs for importing exercises from PDF files (Admin only)")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
public class ExerciseImportController {

    private final RestTemplate restTemplate;
    
    @Value("${KRAFTLOG_IMPORT_SERVICE_URL}")
    private String importServiceUrl;

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
            // Forward request to kraftlog-import service
            log.info("Forwarding PDF import request to import service: {}", importServiceUrl);
            
            // Prepare multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("file", fileResource);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Call import service
            ResponseEntity<Map> response = restTemplate.exchange(
                    importServiceUrl + "/api/import/pdf",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
            
            log.info("Import service response: {}", response.getBody());
            
            return ResponseEntity.status(response.getStatusCode()).body((Map<String, Object>) response.getBody());
            
        } catch (IOException e) {
            log.error("Failed to read PDF file", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to read PDF: " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Failed to import exercises from PDF", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to import exercises: " + e.getMessage()
            ));
        }
    }
}
