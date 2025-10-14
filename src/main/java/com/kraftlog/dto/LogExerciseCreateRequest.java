package com.kraftlog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogExerciseCreateRequest {

    @NotNull(message = "LogWorkout ID is required")
    private UUID logWorkoutId;

    @NotNull(message = "Exercise ID is required")
    private UUID exerciseId;

    private LocalDateTime startDatetime;

    private LocalDateTime endDatetime;

    private String notes;

    private Integer repetitions;

    private Boolean completed;
}
