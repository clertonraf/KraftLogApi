package com.kraftlog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "muscles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Muscle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "muscle_group", nullable = false)
    @Enumerated(EnumType.STRING)
    private MuscleGroup muscleGroup;

    public enum MuscleGroup {
        CHEST, DELTOIDS, SHOULDERS, BICEPS, TRICEPS, BACK, FOREARMS, GLUTES, LEGS, CALVES
    }
}
