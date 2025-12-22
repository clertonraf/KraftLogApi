package com.kraftlog.repository;

import com.kraftlog.entity.LogWorkout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LogWorkoutRepository extends JpaRepository<LogWorkout, UUID> {

    List<LogWorkout> findByLogRoutineId(UUID logRoutineId);

    List<LogWorkout> findByWorkoutId(UUID workoutId);
    
    List<LogWorkout> findByWorkoutIdAndEndDatetimeIsNotNullOrderByEndDatetimeDesc(UUID workoutId);
}
