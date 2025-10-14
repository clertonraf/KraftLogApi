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
@Table(name = "workouts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "interval_minutes")
    private Integer intervalMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @ManyToMany
    @JoinTable(
        name = "workout_exercises",
        joinColumns = @JoinColumn(name = "workout_id"),
        inverseJoinColumns = @JoinColumn(name = "exercise_id")
    )
    @Builder.Default
    private List<Exercise> exercises = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "workout_muscles",
        joinColumns = @JoinColumn(name = "workout_id"),
        inverseJoinColumns = @JoinColumn(name = "muscle_id")
    )
    @Builder.Default
    private List<Muscle> muscles = new ArrayList<>();
}
