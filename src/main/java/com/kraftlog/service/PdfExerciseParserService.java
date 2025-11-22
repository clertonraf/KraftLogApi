package com.kraftlog.service;

import com.kraftlog.config.ExerciseImportProperties;
import com.kraftlog.dto.ParsedExerciseData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to parse exercise data from PDF files
 * Extracts exercise names, video URLs, and muscle groups from structured PDF tables
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExerciseParserService {

    private final ExerciseImportProperties importProperties;

    // Pattern to match YouTube URLs (both youtube.com and youtu.be formats)
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https://(?:(?:www\\.)?youtube\\.com/watch\\?v=|youtu\\.be/)[A-Za-z0-9_-]+");

    /**
     * Parse exercises from a PDF file
     * 
     * @param pdfFile the PDF file to parse
     * @return list of parsed exercise data
     * @throws IOException if file reading fails
     */
    public List<ParsedExerciseData> parseExercisesFromPdf(File pdfFile) throws IOException {
        log.info("Parsing exercises from PDF: {}", pdfFile.getName());
        
        List<ParsedExerciseData> exercises = new ArrayList<>();
        
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            exercises = parseExercisesFromText(text);
            
            log.info("Successfully parsed {} exercises from PDF", exercises.size());
        }
        
        return exercises;
    }

    /**
     * Parse exercises from extracted PDF text
     * Expected format:
     * - Muscle group header (e.g., "PEITO")
     * - Table with exercise names and video URLs
     * 
     * @param text the extracted PDF text
     * @return list of parsed exercise data
     */
    private List<ParsedExerciseData> parseExercisesFromText(String text) {
        List<ParsedExerciseData> exercises = new ArrayList<>();
        
        String[] lines = text.split("\n");
        String currentMuscleGroup = null;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            // Check if this line is a muscle group header
            String detectedMuscleGroup = detectMuscleGroup(line);
            if (detectedMuscleGroup != null) {
                currentMuscleGroup = detectedMuscleGroup;
                log.debug("Found muscle group: {}", currentMuscleGroup);
                continue;
            }
            
            // Skip empty lines and headers
            if (line.isEmpty() || line.startsWith("EXERCÍCIO") || line.startsWith("VÍDEO")) {
                continue;
            }
            
            // Try to extract exercise and URL
            if (currentMuscleGroup != null) {
                ParsedExerciseData exercise = parseExerciseLine(line, currentMuscleGroup);
                if (exercise != null) {
                    exercises.add(exercise);
                    log.debug("Parsed exercise: {} - {}", exercise.getName(), exercise.getMuscleGroupPortuguese());
                }
            }
        }
        
        return exercises;
    }

    /**
     * Detect if a line contains a muscle group header
     * Uses configured muscle group mappings from application.yml
     * 
     * @param line the line to check
     * @return the detected muscle group in Portuguese, or null
     */
    private String detectMuscleGroup(String line) {
        String upperLine = line.toUpperCase();
        
        // Get configured muscle group headers from properties
        Set<String> configuredHeaders = importProperties.getMuscleGroupMapping().keySet();
        
        for (String header : configuredHeaders) {
            if (upperLine.contains(header.toUpperCase())) {
                return header;
            }
        }
        
        return null;
    }

    /**
     * Parse a single line containing exercise name and video URL
     * Handles various formats:
     * - "Exercise Name https://youtu.be/xxx"
     * - "Exercise Name https://youtube.com/watch?v=xxx"
     * - "Exercise Name" (no URL)
     * 
     * @param line the line to parse
     * @param muscleGroup the current muscle group
     * @return parsed exercise data, or null if parsing fails
     */
    private ParsedExerciseData parseExerciseLine(String line, String muscleGroup) {
        // Extract video URL
        Matcher urlMatcher = URL_PATTERN.matcher(line);
        String videoUrl = null;
        String exerciseName;
        
        if (urlMatcher.find()) {
            videoUrl = urlMatcher.group();
            // Extract exercise name (text before the URL)
            int urlStart = line.indexOf(videoUrl);
            exerciseName = line.substring(0, urlStart).trim();
            
            log.debug("Found URL: {}", videoUrl);
        } else {
            // No URL found, use entire line as exercise name
            exerciseName = line.trim();
        }
        
        // Clean up exercise name
        exerciseName = cleanExerciseName(exerciseName);
        
        // Only create exercise if we have a valid name
        if (exerciseName.isEmpty() || exerciseName.length() < 3) {
            return null;
        }
        
        // Validate exercise name doesn't contain URL fragments
        if (exerciseName.contains("http") || exerciseName.contains("youtu")) {
            log.warn("Exercise name still contains URL fragments: {}", exerciseName);
            return null;
        }
        
        return ParsedExerciseData.builder()
                .name(exerciseName)
                .videoUrl(videoUrl)
                .muscleGroupPortuguese(muscleGroup)
                .build();
    }

    /**
     * Clean exercise name by removing unwanted characters and formatting
     * 
     * @param name the raw exercise name
     * @return cleaned exercise name
     */
    private String cleanExerciseName(String name) {
        // Remove extra whitespace
        name = name.replaceAll("\\s+", " ").trim();
        
        // Remove common table artifacts
        name = name.replaceAll("^[\\d.\\-]+\\s*", ""); // Remove leading numbers/dots
        name = name.replaceAll("[|\\t]+", " "); // Remove pipes and tabs
        
        return name.trim();
    }
}
