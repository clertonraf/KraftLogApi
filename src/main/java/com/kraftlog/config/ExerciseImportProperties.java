package com.kraftlog.config;

import com.kraftlog.entity.Muscle;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for exercise import feature
 * Loads muscle group mappings from an external YAML file
 */
@Configuration
@Data
@Slf4j
public class ExerciseImportProperties {

    /**
     * Path to the external muscle group mapping configuration file
     * Can be configured via environment variable: EXERCISE_MUSCLE_GROUPS_CONFIG_PATH
     * If not specified or file not found, exercises will be imported without muscle group associations
     */
    @Value("${exercise.muscle-groups.config-path:#{null}}")
    private String configPath;

    /**
     * Mapping from Portuguese muscle group names to English MuscleGroup enum values
     * Loaded from external YAML file at startup
     */
    private Map<String, String> muscleGroupMapping = new HashMap<>();

    @PostConstruct
    public void loadConfiguration() {
        if (configPath == null || configPath.trim().isEmpty()) {
            log.warn("No exercise muscle groups configuration file specified. " +
                    "Set 'exercise.muscle-groups.config-path' or environment variable 'EXERCISE_MUSCLE_GROUPS_CONFIG_PATH'. " +
                    "Exercises will be imported without muscle group associations.");
            return;
        }

        try {
            log.info("Loading exercise muscle groups configuration from: {}", configPath);
            
            try (InputStream inputStream = new FileInputStream(configPath)) {
                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.load(inputStream);
                
                if (data != null) {
                    data.forEach((key, value) -> {
                        if (value != null) {
                            muscleGroupMapping.put(key, value.toString());
                        }
                    });
                    
                    log.info("Successfully loaded {} muscle group mappings from configuration file", 
                            muscleGroupMapping.size());
                    
                    if (log.isDebugEnabled()) {
                        muscleGroupMapping.forEach((pt, en) -> 
                            log.debug("  Mapping: {} -> {}", pt, en));
                    }
                } else {
                    log.warn("Configuration file is empty or invalid: {}", configPath);
                }
            }
            
        } catch (IOException e) {
            log.warn("Could not load exercise muscle groups configuration from '{}': {}. " +
                    "Exercises will be imported without muscle group associations.", 
                    configPath, e.getMessage());
        }
    }

    /**
     * Get the MuscleGroup enum value for a Portuguese muscle group name
     * 
     * @param portugueseName the Portuguese name (e.g., "PEITO", "BÍCEPS")
     * @return the corresponding MuscleGroup enum value, or null if not found or not configured
     */
    public Muscle.MuscleGroup getMuscleGroup(String portugueseName) {
        if (portugueseName == null || muscleGroupMapping.isEmpty()) {
            return null;
        }
        
        String englishName = muscleGroupMapping.get(portugueseName.toUpperCase());
        if (englishName == null) {
            return null;
        }
        
        try {
            return Muscle.MuscleGroup.valueOf(englishName.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid MuscleGroup enum value '{}' for Portuguese name '{}'. " +
                    "Valid values are: {}", 
                    englishName, portugueseName, java.util.Arrays.toString(Muscle.MuscleGroup.values()));
            return null;
        }
    }
    
    /**
     * Check if muscle group mapping is configured
     * 
     * @return true if mapping configuration was loaded, false otherwise
     */
    public boolean hasConfiguration() {
        return !muscleGroupMapping.isEmpty();
    }
}
