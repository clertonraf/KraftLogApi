package com.kraftlog.repository;

import com.kraftlog.TestDataBuilder;
import com.kraftlog.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveUser() {
        // Given
        User user = TestDataBuilder.defaultUser().build();

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John");
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        User user = TestDataBuilder.defaultUser().build();
        entityManager.persistAndFlush(user);

        // When
        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John");
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldCheckIfUserExistsByEmail() {
        // Given
        User user = TestDataBuilder.defaultUser().build();
        entityManager.persistAndFlush(user);

        // When
        boolean exists = userRepository.existsByEmail("john.doe@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExistByEmail() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldUpdateUser() {
        // Given
        User user = TestDataBuilder.defaultUser().build();
        entityManager.persistAndFlush(user);

        // When
        user.setWeightKg(80.0);
        User updatedUser = userRepository.save(user);

        // Then
        assertThat(updatedUser.getWeightKg()).isEqualTo(80.0);
    }

    @Test
    void shouldDeleteUser() {
        // Given
        User user = TestDataBuilder.defaultUser().build();
        entityManager.persistAndFlush(user);

        // When
        userRepository.delete(user);
        Optional<User> deletedUser = userRepository.findById(user.getId());

        // Then
        assertThat(deletedUser).isEmpty();
    }
}
