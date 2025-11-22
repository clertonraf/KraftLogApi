package com.kraftlog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutCreateRequest {

    @NotBlank(message = "Workout name is required")
    private String name;

    private Integer orderIndex;

    @Positive(message = "Interval must be positive")
    private Integer intervalMinutes;

    @NotNull(message = "Routine ID is required")
    private UUID routineId;

    private List<WorkoutExerciseRequest> exercises;

    private List<UUID> muscleIds;
}
