package com.kraftlog.dto;

import com.kraftlog.entity.Muscle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MuscleResponse {

    private UUID id;
    private String name;
    private Muscle.MuscleGroup muscleGroup;
}
