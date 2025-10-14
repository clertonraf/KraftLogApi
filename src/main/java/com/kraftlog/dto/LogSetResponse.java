package com.kraftlog.dto;

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
public class LogSetResponse {

    private UUID id;
    private UUID logExerciseId;
    private Integer setNumber;
    private Integer reps;
    private Double weightKg;
    private Integer restTimeSeconds;
    private LocalDateTime timestamp;
    private String notes;
}
