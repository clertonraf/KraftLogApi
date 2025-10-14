package com.kraftlog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineCreateRequest {

    @NotBlank(message = "Routine name is required")
    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isActive;

    @NotNull(message = "User ID is required")
    private UUID userId;
}
