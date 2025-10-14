package com.kraftlog.repository;

import com.kraftlog.entity.LogRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LogRoutineRepository extends JpaRepository<LogRoutine, UUID> {

    List<LogRoutine> findByRoutineIdOrderByStartDatetimeDesc(UUID routineId);
}
