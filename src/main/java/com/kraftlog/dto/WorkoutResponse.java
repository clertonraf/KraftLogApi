package com.kraftlog.dto;

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
public class WorkoutResponse {

    private UUID id;
    private String name;
    private Integer orderIndex;
    private Integer intervalMinutes;
    private UUID routineId;
    private List<WorkoutExerciseResponse> exercises;
    private List<MuscleResponse> muscles;
}
