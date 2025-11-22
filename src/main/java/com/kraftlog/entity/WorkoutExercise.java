package com.kraftlog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "workout_exercises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(WorkoutExercise.WorkoutExerciseId.class)
public class WorkoutExercise {

    @Id
    @Column(name = "workout_id")
    private UUID workoutId;

    @Id
    @Column(name = "exercise_id")
    private UUID exerciseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", insertable = false, updatable = false)
    private Workout workout;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exercise_id", insertable = false, updatable = false)
    private Exercise exercise;

    @Column(name = "recommended_sets")
    private Integer recommendedSets;

    @Column(name = "recommended_reps")
    private Integer recommendedReps;

    @Column(name = "training_technique")
    private String trainingTechnique;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutExerciseId implements Serializable {
        private UUID workoutId;
        private UUID exerciseId;
    }
}
