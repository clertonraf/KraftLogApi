package com.kraftlog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutExerciseResponse {

    private UUID exerciseId;
    private String exerciseName;
    private String exerciseDescription;
    private String videoUrl;
    private Integer recommendedSets;
    private Integer recommendedReps;
    private String trainingTechnique;
    private Integer orderIndex;
}
