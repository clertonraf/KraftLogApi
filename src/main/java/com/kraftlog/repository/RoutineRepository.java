package com.kraftlog.repository;

import com.kraftlog.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoutineRepository extends JpaRepository<Routine, UUID> {

    List<Routine> findByUserId(UUID userId);

    Optional<Routine> findByUserIdAndIsActiveTrue(UUID userId);
}
