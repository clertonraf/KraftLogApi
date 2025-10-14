package com.kraftlog.dto;

import com.kraftlog.entity.Exercise;
import jakarta.validation.constraints.NotBlank;
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
public class ExerciseCreateRequest {

    @NotBlank(message = "Exercise name is required")
    private String name;

    private String description;

    @Positive(message = "Sets must be positive")
    private Integer sets;

    @Positive(message = "Repetitions must be positive")
    private Integer repetitions;

    private String technique;

    @Positive(message = "Default weight must be positive")
    private Double defaultWeightKg;

    private Exercise.EquipmentType equipmentType;

    private List<UUID> muscleIds;
}
