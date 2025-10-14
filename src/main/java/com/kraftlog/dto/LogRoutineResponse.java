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
public class LogRoutineResponse {

    private UUID id;
    private UUID routineId;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private List<LogWorkoutResponse> logWorkouts;
}
