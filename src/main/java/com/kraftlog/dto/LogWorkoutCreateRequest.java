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
public class LogWorkoutCreateRequest {

    @NotNull(message = "LogRoutine ID is required")
    private UUID logRoutineId;

    @NotNull(message = "Workout ID is required")
    private UUID workoutId;

    @NotNull(message = "Start datetime is required")
    private LocalDateTime startDatetime;

    private LocalDateTime endDatetime;
}
