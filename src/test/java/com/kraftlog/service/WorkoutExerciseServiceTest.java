package com.kraftlog.service;

import com.kraftlog.dto.WorkoutExerciseRequest;
import com.kraftlog.dto.WorkoutExerciseResponse;
import com.kraftlog.entity.Exercise;
import com.kraftlog.entity.Workout;
import com.kraftlog.entity.WorkoutExercise;
import com.kraftlog.exception.ResourceNotFoundException;
import com.kraftlog.repository.ExerciseRepository;
import com.kraftlog.repository.WorkoutExerciseRepository;
import com.kraftlog.repository.WorkoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutExerciseServiceTest {

    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private WorkoutExerciseService workoutExerciseService;

    private UUID workoutId;
    private UUID exerciseId;
    private Workout workout;
    private Exercise exercise;
    private WorkoutExercise workoutExercise;
    private WorkoutExerciseRequest request;

    @BeforeEach
    void setUp() {
        workoutId = UUID.randomUUID();
        exerciseId = UUID.randomUUID();

        workout = Workout.builder()
                .id(workoutId)
                .name("Test Workout")
                .build();

        exercise = Exercise.builder()
                .id(exerciseId)
                .name("Bench Press")
                .description("Chest exercise")
                .videoUrl("https://example.com/video")
                .build();

        workoutExercise = WorkoutExercise.builder()
                .workoutId(workoutId)
                .exerciseId(exerciseId)
                .recommendedSets(3)
                .recommendedReps(10)
                .trainingTechnique("Standard")
                .orderIndex(1)
                .build();

        request = WorkoutExerciseRequest.builder()
                .exerciseId(exerciseId)
                .recommendedSets(3)
                .recommendedReps(10)
                .trainingTechnique("Standard")
                .orderIndex(1)
                .build();
    }

    @Test
    @DisplayName("Should add exercise to workout successfully")
    void shouldAddExerciseToWorkout() {
        // Given
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(workoutExerciseRepository.save(any(WorkoutExercise.class))).thenReturn(workoutExercise);

        // When
        WorkoutExerciseResponse response = workoutExerciseService.addExerciseToWorkout(workoutId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getExerciseId()).isEqualTo(exerciseId);
        assertThat(response.getExerciseName()).isEqualTo("Bench Press");
        assertThat(response.getRecommendedSets()).isEqualTo(3);
        assertThat(response.getRecommendedReps()).isEqualTo(10);
        assertThat(response.getTrainingTechnique()).isEqualTo("Standard");
        assertThat(response.getOrderIndex()).isEqualTo(1);

        ArgumentCaptor<WorkoutExercise> captor = ArgumentCaptor.forClass(WorkoutExercise.class);
        verify(workoutExerciseRepository).save(captor.capture());
        
        WorkoutExercise saved = captor.getValue();
        assertThat(saved.getWorkoutId()).isEqualTo(workoutId);
        assertThat(saved.getExerciseId()).isEqualTo(exerciseId);
        assertThat(saved.getRecommendedSets()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should throw exception when workout not found")
    void shouldThrowExceptionWhenWorkoutNotFound() {
        // Given
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workoutExerciseService.addExerciseToWorkout(workoutId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workout")
                .hasMessageContaining(workoutId.toString());

        verify(exerciseRepository, never()).findById(any());
        verify(workoutExerciseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when exercise not found")
    void shouldThrowExceptionWhenExerciseNotFound() {
        // Given
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workoutExerciseService.addExerciseToWorkout(workoutId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exercise")
                .hasMessageContaining(exerciseId.toString());

        verify(workoutExerciseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get workout exercises successfully")
    void shouldGetWorkoutExercises() {
        // Given
        Exercise exercise2 = Exercise.builder()
                .id(UUID.randomUUID())
                .name("Squat")
                .description("Leg exercise")
                .build();

        WorkoutExercise workoutExercise2 = WorkoutExercise.builder()
                .workoutId(workoutId)
                .exerciseId(exercise2.getId())
                .exercise(exercise2)
                .recommendedSets(4)
                .recommendedReps(8)
                .orderIndex(2)
                .build();

        workoutExercise.setExercise(exercise);
        
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(workoutExerciseRepository.findByWorkoutIdOrderByOrderIndexAsc(workoutId))
                .thenReturn(Arrays.asList(workoutExercise, workoutExercise2));

        // When
        List<WorkoutExerciseResponse> responses = workoutExerciseService.getWorkoutExercises(workoutId);

        // Then
        assertThat(responses).hasSize(2);
        
        WorkoutExerciseResponse first = responses.get(0);
        assertThat(first.getExerciseName()).isEqualTo("Bench Press");
        assertThat(first.getRecommendedSets()).isEqualTo(3);
        assertThat(first.getRecommendedReps()).isEqualTo(10);
        
        WorkoutExerciseResponse second = responses.get(1);
        assertThat(second.getExerciseName()).isEqualTo("Squat");
        assertThat(second.getRecommendedSets()).isEqualTo(4);
        assertThat(second.getRecommendedReps()).isEqualTo(8);
    }

    @Test
    @DisplayName("Should return empty list when workout has no exercises")
    void shouldReturnEmptyListWhenNoExercises() {
        // Given
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(workoutExerciseRepository.findByWorkoutIdOrderByOrderIndexAsc(workoutId))
                .thenReturn(Arrays.asList());

        // When
        List<WorkoutExerciseResponse> responses = workoutExerciseService.getWorkoutExercises(workoutId);

        // Then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when getting exercises for non-existent workout")
    void shouldThrowExceptionWhenGettingExercisesForNonExistentWorkout() {
        // Given
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workoutExerciseService.getWorkoutExercises(workoutId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workout");
    }

    @Test
    @DisplayName("Should remove exercise from workout successfully")
    void shouldRemoveExerciseFromWorkout() {
        // Given
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));

        // When
        workoutExerciseService.removeExerciseFromWorkout(workoutId, exerciseId);

        // Then
        verify(workoutExerciseRepository).deleteByWorkoutIdAndExerciseId(workoutId, exerciseId);
    }

    @Test
    @DisplayName("Should throw exception when removing exercise from non-existent workout")
    void shouldThrowExceptionWhenRemovingFromNonExistentWorkout() {
        // Given
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workoutExerciseService.removeExerciseFromWorkout(workoutId, exerciseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Workout");

        verify(workoutExerciseRepository, never()).deleteByWorkoutIdAndExerciseId(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent exercise")
    void shouldThrowExceptionWhenRemovingNonExistentExercise() {
        // Given
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workout));
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workoutExerciseService.removeExerciseFromWorkout(workoutId, exerciseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exercise");

        verify(workoutExerciseRepository, never()).deleteByWorkoutIdAndExerciseId(any(), any());
    }
}
