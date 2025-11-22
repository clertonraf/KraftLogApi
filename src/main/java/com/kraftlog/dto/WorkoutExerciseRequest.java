package com.kraftlog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutExerciseRequest {

    @NotNull(message = "Exercise ID is required")
    private UUID exerciseId;

    @Positive(message = "Recommended sets must be positive")
    private Integer recommendedSets;

    @Positive(message = "Recommended reps must be positive")
    private Integer recommendedReps;

    private String trainingTechnique;

    private Integer orderIndex;
}
