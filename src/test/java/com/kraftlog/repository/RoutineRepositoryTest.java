package com.kraftlog.repository;

import com.kraftlog.TestDataBuilder;
import com.kraftlog.entity.Routine;
import com.kraftlog.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RoutineRepositoryTest {

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;

    @BeforeEach
    void setUp() {
        user = TestDataBuilder.defaultUser().build();
        entityManager.persistAndFlush(user);
    }

    @Test
    void shouldSaveRoutine() {
        // Given
        Routine routine = TestDataBuilder.defaultRoutine(user).build();

        // When
        Routine savedRoutine = routineRepository.save(routine);

        // Then
        assertThat(savedRoutine.getId()).isNotNull();
        assertThat(savedRoutine.getName()).isEqualTo("Push Pull Legs");
        assertThat(savedRoutine.getUser()).isEqualTo(user);
    }

    @Test
    void shouldFindRoutinesByUserId() {
        // Given
        Routine routine1 = TestDataBuilder.defaultRoutine(user)
                .name("Routine 1")
                .build();
        Routine routine2 = TestDataBuilder.defaultRoutine(user)
                .name("Routine 2")
                .build();
        entityManager.persistAndFlush(routine1);
        entityManager.persistAndFlush(routine2);

        // When
        List<Routine> routines = routineRepository.findByUserId(user.getId());

        // Then
        assertThat(routines).hasSize(2);
        assertThat(routines).extracting(Routine::getName)
                .containsExactlyInAnyOrder("Routine 1", "Routine 2");
    }

    @Test
    void shouldFindActiveRoutineByUserId() {
        // Given
        Routine activeRoutine = TestDataBuilder.defaultRoutine(user)
                .name("Active Routine")
                .isActive(true)
                .build();
        Routine inactiveRoutine = TestDataBuilder.defaultRoutine(user)
                .name("Inactive Routine")
                .isActive(false)
                .build();
        entityManager.persistAndFlush(activeRoutine);
        entityManager.persistAndFlush(inactiveRoutine);

        // When
        Optional<Routine> foundRoutine = routineRepository.findByUserIdAndIsActiveTrue(user.getId());

        // Then
        assertThat(foundRoutine).isPresent();
        assertThat(foundRoutine.get().getName()).isEqualTo("Active Routine");
        assertThat(foundRoutine.get().getIsActive()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenNoActiveRoutineExists() {
        // Given
        Routine inactiveRoutine = TestDataBuilder.defaultRoutine(user)
                .isActive(false)
                .build();
        entityManager.persistAndFlush(inactiveRoutine);

        // When
        Optional<Routine> foundRoutine = routineRepository.findByUserIdAndIsActiveTrue(user.getId());

        // Then
        assertThat(foundRoutine).isEmpty();
    }
}
