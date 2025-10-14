package com.kraftlog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogExerciseResponse {

    private UUID id;
    private UUID logWorkoutId;
    private UUID exerciseId;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String notes;
    private Integer repetitions;
    private Boolean completed;
    private List<LogSetResponse> logSets;
}
