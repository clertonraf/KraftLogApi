package com.kraftlog.service;

import com.kraftlog.TestDataBuilder;
import com.kraftlog.dto.UserCreateRequest;
import com.kraftlog.dto.UserResponse;
import com.kraftlog.dto.UserUpdateRequest;
import com.kraftlog.entity.User;
import com.kraftlog.exception.BadRequestException;
import com.kraftlog.exception.ResourceNotFoundException;
import com.kraftlog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = TestDataBuilder.defaultUser().build();

        createRequest = UserCreateRequest.builder()
                .name("John")
                .surname("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("john.doe@example.com")
                .password("password123")
                .weightKg(75.5)
                .heightCm(180.0)
                .fitnessGoal(User.FitnessGoal.HYPERTROPHY)
                .build();

        updateRequest = UserUpdateRequest.builder()
                .weightKg(80.0)
                .heightCm(182.0)
                .build();

        userResponse = UserResponse.builder()
                .id(user.getId())
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .weightKg(75.5)
                .heightCm(180.0)
                .fitnessGoal(User.FitnessGoal.HYPERTROPHY)
                .build();
    }

    @Test
    void shouldCreateUser() {
        // Given
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(modelMapper.map(createRequest, User.class)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserResponse.class)).thenReturn(userResponse);

        // When
        UserResponse result = userService.createUser(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        verify(userRepository).existsByEmail(createRequest.getEmail());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithExistingEmail() {
        // Given
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(userRepository).existsByEmail(createRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldGetUserById() {
        // Given
        UUID userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserResponse.class)).thenReturn(userResponse);

        // When
        UserResponse result = userService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldGetUserByEmail() {
        // Given
        String email = "john.doe@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserResponse.class)).thenReturn(userResponse);

        // When
        UserResponse result = userService.getUserByEmail(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldGetAllUsers() {
        // Given
        User user2 = TestDataBuilder.defaultUser()
                .email("jane.doe@example.com")
                .build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));
        when(modelMapper.map(any(User.class), eq(UserResponse.class)))
                .thenReturn(userResponse);

        // When
        List<UserResponse> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    void shouldUpdateUser() {
        // Given
        UUID userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserResponse.class)).thenReturn(userResponse);

        // When
        UserResponse result = userService.updateUser(userId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        assertThat(user.getWeightKg()).isEqualTo(80.0);
        assertThat(user.getHeightCm()).isEqualTo(182.0);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldDeleteUser() {
        // Given
        UUID userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }
}
