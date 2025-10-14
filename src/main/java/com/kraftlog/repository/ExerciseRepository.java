package com.kraftlog.repository;

import com.kraftlog.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    List<Exercise> findByNameContainingIgnoreCase(String name);

    @Query("SELECT e FROM Exercise e JOIN e.muscles m WHERE m.id = :muscleId")
    List<Exercise> findByMuscleId(@Param("muscleId") UUID muscleId);
}
