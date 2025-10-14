package com.kraftlog.repository;

import com.kraftlog.entity.Muscle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MuscleRepository extends JpaRepository<Muscle, UUID> {

    Optional<Muscle> findByName(String name);
}
