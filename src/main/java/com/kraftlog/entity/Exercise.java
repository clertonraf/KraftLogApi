package com.kraftlog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
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

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    private Integer sets;

    private Integer repetitions;

    @Column(length = 1000)
    private String technique;

    @Column(name = "default_weight_kg")
    private Double defaultWeightKg;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum EquipmentType {
        BARBELL, DUMBBELL, MACHINE, SMITH_MACHINE, CABLE, BODYWEIGHT, KETTLEBELL, RESISTANCE_BAND, OTHER
    }
}
