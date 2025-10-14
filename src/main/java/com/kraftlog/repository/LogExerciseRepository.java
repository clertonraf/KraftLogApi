package com.kraftlog.repository;

import com.kraftlog.entity.LogExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LogExerciseRepository extends JpaRepository<LogExercise, UUID> {

    List<LogExercise> findByLogWorkoutId(UUID logWorkoutId);

    List<LogExercise> findByExerciseId(UUID exerciseId);
}
