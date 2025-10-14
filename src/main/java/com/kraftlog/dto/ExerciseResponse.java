package com.kraftlog.dto;

import com.kraftlog.entity.Exercise;
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
public class ExerciseResponse {

    private UUID id;
    private String name;
    private String description;
    private Integer sets;
    private Integer repetitions;
    private String technique;
    private Double defaultWeightKg;
    private Exercise.EquipmentType equipmentType;
    private List<MuscleResponse> muscles;
}
