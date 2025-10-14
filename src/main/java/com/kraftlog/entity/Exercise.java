package com.kraftlog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "exercises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private Integer sets;

    private Integer repetitions;

    @Column(length = 1000)
    private String technique;

    @Column(name = "default_weight_kg")
    private Double defaultWeightKg;

    @Column(name = "equipment_type")
    @Enumerated(EnumType.STRING)
    private EquipmentType equipmentType;

    @ManyToMany
    @JoinTable(
        name = "exercise_muscles",
        joinColumns = @JoinColumn(name = "exercise_id"),
        inverseJoinColumns = @JoinColumn(name = "muscle_id")
    )
    @Builder.Default
    private List<Muscle> muscles = new ArrayList<>();

    public enum EquipmentType {
        BARBELL, DUMBBELL, MACHINE, SMITH_MACHINE, CABLE, BODYWEIGHT, KETTLEBELL, RESISTANCE_BAND, OTHER
    }
}
