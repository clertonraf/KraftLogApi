package com.kraftlog.repository;

import com.kraftlog.entity.LogSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LogSetRepository extends JpaRepository<LogSet, UUID> {

    List<LogSet> findByLogExerciseIdOrderBySetNumberAsc(UUID logExerciseId);
}
