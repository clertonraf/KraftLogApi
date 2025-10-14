package com.kraftlog.dto;

import jakarta.validation.constraints.NotBlank;
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
public class AerobicActivityCreateRequest {

    @NotBlank(message = "Activity name is required")
    private String name;

    @Positive(message = "Duration must be positive")
    private Integer durationMinutes;

    private String notes;

    @NotNull(message = "Routine ID is required")
    private UUID routineId;
}
