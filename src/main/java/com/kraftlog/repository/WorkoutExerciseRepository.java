package com.kraftlog.repository;

import com.kraftlog.entity.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, WorkoutExercise.WorkoutExerciseId> {

    List<WorkoutExercise> findByWorkoutIdOrderByOrderIndexAsc(UUID workoutId);
    
    void deleteByWorkoutIdAndExerciseId(UUID workoutId, UUID exerciseId);
}
