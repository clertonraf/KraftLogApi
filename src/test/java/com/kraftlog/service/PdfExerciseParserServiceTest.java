package com.kraftlog.service;

import com.kraftlog.dto.ParsedExerciseData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "exercise.muscle-groups.config-path=exercise-muscle-groups.yml"
})
class PdfExerciseParserServiceTest {

    @Autowired
    private PdfExerciseParserService parserService;

    @Test
    @DisplayName("Should parse exercises from real PDF if it exists")
    void shouldParseExercisesFromPdf() throws Exception {
        File pdfFile = new File("tmp/lista-de-videos-de-exercicios.pdf");
        
        if (!pdfFile.exists()) {
            System.out.println("⚠️  PDF file not found, skipping test");
            return;
        }
        
        // When
        List<ParsedExerciseData> exercises = parserService.parseExercisesFromPdf(pdfFile);
        
        // Then
        assertThat(exercises).isNotEmpty();
        
        // Print some statistics
        System.out.println("✅ Successfully parsed " + exercises.size() + " exercises");
        
        // Group by muscle group and count
        exercises.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                ParsedExerciseData::getMuscleGroupPortuguese,
                java.util.stream.Collectors.counting()
            ))
            .forEach((group, count) -> 
                System.out.println("  - " + group + ": " + count + " exercises")
            );
        
        // Verify structure
        ParsedExerciseData firstExercise = exercises.get(0);
        assertThat(firstExercise.getName()).isNotBlank();
        assertThat(firstExercise.getMuscleGroupPortuguese()).isNotNull();
        
        System.out.println("\nExample exercise:");
        System.out.println("  Name: " + firstExercise.getName());
        System.out.println("  Muscle Group: " + firstExercise.getMuscleGroupPortuguese());
        System.out.println("  Video URL: " + (firstExercise.getVideoUrl() != null ? firstExercise.getVideoUrl() : "N/A"));
    }
}
