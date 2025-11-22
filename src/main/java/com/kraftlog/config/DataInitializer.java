package com.kraftlog.config;

import com.kraftlog.entity.Muscle;
import com.kraftlog.repository.MuscleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Initializes essential data on application startup
 * Ensures muscle groups are always available in the database
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MuscleRepository muscleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking muscle groups data...");
        
        long muscleCount = muscleRepository.count();
        
        if (muscleCount == 0) {
            log.info("No muscles found in database. Initializing muscle groups...");
            initializeMuscles();
        } else {
            log.info("Muscle groups already initialized. Found {} muscles.", muscleCount);
        }
    }

    private void initializeMuscles() {
        List<Muscle> muscles = Arrays.asList(
            // CHEST
            Muscle.builder().name("Pectoralis Major").muscleGroup(Muscle.MuscleGroup.CHEST).build(),
            Muscle.builder().name("Pectoralis Minor").muscleGroup(Muscle.MuscleGroup.CHEST).build(),
            
            // DELTOIDS
            Muscle.builder().name("Anterior Deltoid").muscleGroup(Muscle.MuscleGroup.DELTOIDS).build(),
            Muscle.builder().name("Lateral Deltoid").muscleGroup(Muscle.MuscleGroup.DELTOIDS).build(),
            Muscle.builder().name("Posterior Deltoid").muscleGroup(Muscle.MuscleGroup.DELTOIDS).build(),
            
            // SHOULDERS
            Muscle.builder().name("Trapezius").muscleGroup(Muscle.MuscleGroup.SHOULDERS).build(),
            
            // BICEPS
            Muscle.builder().name("Biceps Brachii").muscleGroup(Muscle.MuscleGroup.BICEPS).build(),
            Muscle.builder().name("Brachialis").muscleGroup(Muscle.MuscleGroup.BICEPS).build(),
            
            // TRICEPS
            Muscle.builder().name("Triceps Brachii").muscleGroup(Muscle.MuscleGroup.TRICEPS).build(),
            
            // BACK
            Muscle.builder().name("Latissimus Dorsi").muscleGroup(Muscle.MuscleGroup.BACK).build(),
            Muscle.builder().name("Rhomboids").muscleGroup(Muscle.MuscleGroup.BACK).build(),
            Muscle.builder().name("Erector Spinae").muscleGroup(Muscle.MuscleGroup.BACK).build(),
            
            // FOREARMS
            Muscle.builder().name("Forearm Flexors").muscleGroup(Muscle.MuscleGroup.FOREARMS).build(),
            Muscle.builder().name("Forearm Extensors").muscleGroup(Muscle.MuscleGroup.FOREARMS).build(),
            
            // GLUTES
            Muscle.builder().name("Gluteus Maximus").muscleGroup(Muscle.MuscleGroup.GLUTES).build(),
            Muscle.builder().name("Gluteus Medius").muscleGroup(Muscle.MuscleGroup.GLUTES).build(),
            
            // LEGS
            Muscle.builder().name("Quadriceps").muscleGroup(Muscle.MuscleGroup.LEGS).build(),
            Muscle.builder().name("Hamstrings").muscleGroup(Muscle.MuscleGroup.LEGS).build(),
            Muscle.builder().name("Adductors").muscleGroup(Muscle.MuscleGroup.LEGS).build(),
            Muscle.builder().name("Abductors").muscleGroup(Muscle.MuscleGroup.LEGS).build(),
            
            // CALVES
            Muscle.builder().name("Gastrocnemius").muscleGroup(Muscle.MuscleGroup.CALVES).build(),
            Muscle.builder().name("Soleus").muscleGroup(Muscle.MuscleGroup.CALVES).build()
        );

        muscleRepository.saveAll(muscles);
        log.info("Successfully initialized {} muscle groups", muscles.size());
    }
}
