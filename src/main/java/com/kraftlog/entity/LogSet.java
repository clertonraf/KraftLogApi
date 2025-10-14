package com.kraftlog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "log_sets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogSet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_exercise_id", nullable = false)
    private LogExercise logExercise;

    @Column(name = "set_number", nullable = false)
    private Integer setNumber;

    private Integer reps;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "rest_time_seconds")
    private Integer restTimeSeconds;

    private LocalDateTime timestamp;

    private String notes;
}
