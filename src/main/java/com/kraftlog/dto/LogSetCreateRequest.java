package com.kraftlog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class LogSetCreateRequest {

    @NotNull(message = "LogExercise ID is required")
    private UUID logExerciseId;

    @NotNull(message = "Set number is required")
    @Positive(message = "Set number must be positive")
    private Integer setNumber;

    private Integer reps;

    private Double weightKg;

    private Integer restTimeSeconds;

    private LocalDateTime timestamp;

    private String notes;
}
