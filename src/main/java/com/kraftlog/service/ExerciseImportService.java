package com.kraftlog.service;

import com.kraftlog.config.ExerciseImportProperties;
import com.kraftlog.dto.ParsedExerciseData;
import com.kraftlog.entity.Exercise;
import com.kraftlog.entity.Muscle;
import com.kraftlog.exception.BadRequestException;
import com.kraftlog.repository.ExerciseRepository;
import com.kraftlog.repository.MuscleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Service to import exercises from PDF files into the database
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExerciseImportService {

    private final PdfExerciseParserService pdfParser;
    private final ExerciseRepository exerciseRepository;
    private final MuscleRepository muscleRepository;
    private final ExerciseImportProperties importProperties;

    /**
     * Import exercises from a PDF file
     * 
     * @param pdfFile the PDF file to import from
     * @return import result with statistics
     * @throws IOException if file reading fails
     */
    public ImportResult importExercisesFromPdf(File pdfFile) throws IOException {
        log.info("Starting exercise import from PDF: {}", pdfFile.getName());
        
        // Parse exercises from PDF
        List<ParsedExerciseData> parsedExercises = pdfParser.parseExercisesFromPdf(pdfFile);
        
        if (parsedExercises.isEmpty()) {
            throw new BadRequestException("No exercises found in PDF file");
        }
        
        // Import exercises
        ImportResult result = ImportResult.builder().build();
        
        for (ParsedExerciseData parsedExercise : parsedExercises) {
            try {
                importSingleExercise(parsedExercise);
                result.incrementSuccess();
            } catch (Exception e) {
                log.warn("Failed to import exercise: {} - {}", parsedExercise.getName(), e.getMessage());
                result.addFailure(parsedExercise.getName(), e.getMessage());
            }
        }
        
        log.info("Exercise import completed. Success: {}, Failed: {}", 
                result.getSuccessCount(), result.getFailureCount());
        
        return result;
    }

    /**
     * Import a single parsed exercise (upsert behavior)
     * If exercise exists by name, updates it; otherwise creates new
     * 
     * @param parsedExercise the parsed exercise data
     */
    private void importSingleExercise(ParsedExerciseData parsedExercise) {
        // Find existing exercise by name or create new
        Exercise exercise = exerciseRepository.findByName(parsedExercise.getName())
                .orElse(Exercise.builder()
                        .name(parsedExercise.getName())
                        .muscles(new ArrayList<>())
                        .build());
        
        // Update video URL if provided
        if (parsedExercise.getVideoUrl() != null && !parsedExercise.getVideoUrl().isEmpty()) {
            exercise.setVideoUrl(parsedExercise.getVideoUrl());
        }
        
        // Update muscle associations based on muscle group
        Muscle.MuscleGroup muscleGroup = translateMuscleGroup(parsedExercise.getMuscleGroupPortuguese());
        if (muscleGroup != null) {
            List<Muscle> muscles = muscleRepository.findByMuscleGroup(muscleGroup);
            if (!muscles.isEmpty()) {
                // Only update muscles if they're not already set or if the new list is different
                if (exercise.getMuscles() == null || exercise.getMuscles().isEmpty()) {
                    exercise.setMuscles(muscles);
                } else {
                    // Merge: add new muscles that aren't already associated
                    List<UUID> existingMuscleIds = exercise.getMuscles().stream()
                            .map(Muscle::getId)
                            .toList();
                    
                    List<Muscle> newMuscles = muscles.stream()
                            .filter(m -> !existingMuscleIds.contains(m.getId()))
                            .toList();
                    
                    if (!newMuscles.isEmpty()) {
                        exercise.getMuscles().addAll(newMuscles);
                    }
                }
            }
        }
        
        exerciseRepository.save(exercise);
        
        if (exercise.getId() != null) {
            log.debug("Updated existing exercise: {}", exercise.getName());
        } else {
            log.debug("Created new exercise: {}", exercise.getName());
        }
    }

    /**
     * Translate Portuguese muscle group name to enum value using configuration
     * Returns null if configuration is not loaded or muscle group not found
     * 
     * @param portugueseName the Portuguese muscle group name
     * @return the corresponding MuscleGroup enum value, or null if not found or not configured
     */
    private Muscle.MuscleGroup translateMuscleGroup(String portugueseName) {
        if (portugueseName == null) {
            return null;
        }
        
        if (!importProperties.hasConfiguration()) {
            log.debug("No muscle group configuration loaded. Exercise '{}' will be imported without muscle group association.", portugueseName);
            return null;
        }
        
        Muscle.MuscleGroup group = importProperties.getMuscleGroup(portugueseName);
        
        if (group == null) {
            log.warn("Unknown muscle group '{}'. Check your exercise muscle groups configuration file.", portugueseName);
        }
        
        return group;
    }

    /**
     * Result of an import operation
     */
    @lombok.Data
    @lombok.Builder
    public static class ImportResult {
        @lombok.Builder.Default
        private int successCount = 0;
        
        @lombok.Builder.Default
        private List<ImportFailure> failures = new ArrayList<>();
        
        public void incrementSuccess() {
            successCount++;
        }
        
        public void addFailure(String exerciseName, String reason) {
            failures.add(new ImportFailure(exerciseName, reason));
        }
        
        public int getFailureCount() {
            return failures.size();
        }
        
        public int getTotalCount() {
            return successCount + failures.size();
        }
    }

    /**
     * Details of a failed import
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ImportFailure {
        private String exerciseName;
        private String reason;
    }
}
