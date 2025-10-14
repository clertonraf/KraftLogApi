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
public class LogRoutineCreateRequest {

    @NotNull(message = "Routine ID is required")
    private UUID routineId;

    @NotNull(message = "Start datetime is required")
    private LocalDateTime startDatetime;

    private LocalDateTime endDatetime;
}
