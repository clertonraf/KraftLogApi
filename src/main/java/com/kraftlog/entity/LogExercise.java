package com.kraftlog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "log_exercises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_workout_id", nullable = false)
    private LogWorkout logWorkout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "start_datetime")
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime")
    private LocalDateTime endDatetime;

    private String notes;

    private Integer repetitions;

    private Boolean completed;

    @OneToMany(mappedBy = "logExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LogSet> logSets = new ArrayList<>();
}
