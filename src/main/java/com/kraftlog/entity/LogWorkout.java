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
@Table(name = "log_workouts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogWorkout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_routine_id", nullable = false)
    private LogRoutine logRoutine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    @Column(name = "end_datetime")
    private LocalDateTime endDatetime;

    @OneToMany(mappedBy = "logWorkout", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LogExercise> logExercises = new ArrayList<>();
}
