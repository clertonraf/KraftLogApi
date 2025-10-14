package com.kraftlog.repository;

import com.kraftlog.TestDataBuilder;
import com.kraftlog.entity.Exercise;
import com.kraftlog.entity.Muscle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ExerciseRepositoryTest {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Muscle muscle;

    @BeforeEach
    void setUp() {
        muscle = TestDataBuilder.defaultMuscle().build();
        entityManager.persistAndFlush(muscle);
    }

    @Test
    void shouldSaveExerciseWithMuscles() {
        // Given
        Exercise exercise = TestDataBuilder.defaultExercise().build();
        exercise.getMuscles().add(muscle);

        // When
        Exercise savedExercise = exerciseRepository.save(exercise);

        // Then
        assertThat(savedExercise.getId()).isNotNull();
        assertThat(savedExercise.getName()).isEqualTo("Bench Press");
        assertThat(savedExercise.getMuscles()).hasSize(1);
        assertThat(savedExercise.getMuscles().get(0).getName()).isEqualTo("Pectoralis Major");
    }

    @Test
    void shouldFindExercisesByNameContaining() {
        // Given
        Exercise exercise1 = TestDataBuilder.defaultExercise()
                .name("Bench Press")
                .build();
        Exercise exercise2 = TestDataBuilder.defaultExercise()
                .name("Incline Bench Press")
                .build();
        entityManager.persistAndFlush(exercise1);
        entityManager.persistAndFlush(exercise2);

        // When
        List<Exercise> exercises = exerciseRepository.findByNameContainingIgnoreCase("bench");

        // Then
        assertThat(exercises).hasSize(2);
        assertThat(exercises).extracting(Exercise::getName)
                .containsExactlyInAnyOrder("Bench Press", "Incline Bench Press");
    }

    @Test
    void shouldFindExercisesByMuscleId() {
        // Given
        Exercise exercise = TestDataBuilder.defaultExercise().build();
        exercise.getMuscles().add(muscle);
        entityManager.persistAndFlush(exercise);

        // When
        List<Exercise> exercises = exerciseRepository.findByMuscleId(muscle.getId());

        // Then
        assertThat(exercises).hasSize(1);
        assertThat(exercises.get(0).getName()).isEqualTo("Bench Press");
        assertThat(exercises.get(0).getMuscles()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListWhenNoExercisesMatchMuscle() {
        // When
        List<Exercise> exercises = exerciseRepository.findByMuscleId(muscle.getId());

        // Then
        assertThat(exercises).isEmpty();
    }
}
