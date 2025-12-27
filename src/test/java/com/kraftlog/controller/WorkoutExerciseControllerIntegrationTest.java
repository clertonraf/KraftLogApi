package com.kraftlog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraftlog.dto.*;
import com.kraftlog.entity.User;
import com.kraftlog.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WorkoutExerciseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private MuscleRepository muscleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private UUID userId;
    private UUID exerciseId;
    private UUID routineId;
    private UUID workoutId;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        workoutExerciseRepository.deleteAll();
        workoutRepository.deleteAll();
        routineRepository.deleteAll();
        exerciseRepository.deleteAll();
        muscleRepository.deleteAll();
        userRepository.deleteAll();

        // Create and authenticate user (admin)
        User user = User.builder()
                .name("Test")
                .surname("User")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .birthDate(LocalDate.of(1990, 1, 1))
                .isAdmin(true)
                .build();
        user = userRepository.save(user);
        userId = user.getId();

        // Login and get token
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        authToken = objectMapper.readTree(responseBody).get("token").asText();

        // Create an exercise
        ExerciseCreateRequest exerciseRequest = ExerciseCreateRequest.builder()
                .name("Bench Press")
                .description("Chest exercise")
                .sets(3)
                .repetitions(10)
                .equipmentType(com.kraftlog.entity.Exercise.EquipmentType.BARBELL)
                .muscleIds(List.of())
                .build();

        result = mockMvc.perform(post("/api/exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exerciseRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        responseBody = result.getResponse().getContentAsString();
        exerciseId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        // Create a routine
        RoutineCreateRequest routineRequest = RoutineCreateRequest.builder()
                .name("Test Routine")
                .userId(userId)
                .build();

        result = mockMvc.perform(post("/api/routines")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routineRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        responseBody = result.getResponse().getContentAsString();
        routineId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        // Create a workout
        WorkoutCreateRequest workoutRequest = WorkoutCreateRequest.builder()
                .name("Test Workout")
                .routineId(routineId)
                .orderIndex(1)
                .build();

        result = mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workoutRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        responseBody = result.getResponse().getContentAsString();
        workoutId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
    }

    @Test
    @DisplayName("Should add exercise to workout successfully")
    void shouldAddExerciseToWorkout() throws Exception {
        // Given
        WorkoutExerciseController.WorkoutExerciseAddRequest request = 
                WorkoutExerciseController.WorkoutExerciseAddRequest.builder()
                        .workoutId(workoutId)
                        .exerciseId(exerciseId)
                        .recommendedSets(4)
                        .recommendedReps(12)
                        .trainingTechnique("Drop sets")
                        .orderIndex(1)
                        .build();

        // When & Then
        mockMvc.perform(post("/api/workout-exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseId").value(exerciseId.toString()))
                .andExpect(jsonPath("$.exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$.exerciseDescription").value("Chest exercise"))
                .andExpect(jsonPath("$.recommendedSets").value(4))
                .andExpect(jsonPath("$.recommendedReps").value(12))
                .andExpect(jsonPath("$.trainingTechnique").value("Drop sets"))
                .andExpect(jsonPath("$.orderIndex").value(1));
    }

    @Test
    @DisplayName("Should return 404 when adding exercise to non-existent workout")
    void shouldReturn404WhenWorkoutNotFound() throws Exception {
        // Given
        UUID nonExistentWorkoutId = UUID.randomUUID();
        WorkoutExerciseController.WorkoutExerciseAddRequest request = 
                WorkoutExerciseController.WorkoutExerciseAddRequest.builder()
                        .workoutId(nonExistentWorkoutId)
                        .exerciseId(exerciseId)
                        .recommendedSets(3)
                        .recommendedReps(10)
                        .build();

        // When & Then
        mockMvc.perform(post("/api/workout-exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when adding non-existent exercise")
    void shouldReturn404WhenExerciseNotFound() throws Exception {
        // Given
        UUID nonExistentExerciseId = UUID.randomUUID();
        WorkoutExerciseController.WorkoutExerciseAddRequest request = 
                WorkoutExerciseController.WorkoutExerciseAddRequest.builder()
                        .workoutId(workoutId)
                        .exerciseId(nonExistentExerciseId)
                        .recommendedSets(3)
                        .recommendedReps(10)
                        .build();

        // When & Then
        mockMvc.perform(post("/api/workout-exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when request has validation errors")
    void shouldReturn400WhenValidationFails() throws Exception {
        // Given - missing required fields
        WorkoutExerciseController.WorkoutExerciseAddRequest request = 
                WorkoutExerciseController.WorkoutExerciseAddRequest.builder()
                        .recommendedSets(3)
                        .recommendedReps(10)
                        .build();

        // When & Then
        mockMvc.perform(post("/api/workout-exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all exercises in a workout")
    void shouldGetWorkoutExercises() throws Exception {
        // Given - add two exercises to workout
        WorkoutExerciseController.WorkoutExerciseAddRequest request1 = 
                WorkoutExerciseController.WorkoutExerciseAddRequest.builder()
                        .workoutId(workoutId)
                        .exerciseId(exerciseId)
                        .recommendedSets(3)
                        .recommendedReps(10)
                        .orderIndex(1)
                        .build();

        mockMvc.perform(post("/api/workout-exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Create second exercise
        ExerciseCreateRequest exerciseRequest2 = ExerciseCreateRequest.builder()
                .name("Squat")
                .description("Leg exercise")
                .sets(4)
                .repetitions(8)
                .equipmentType(com.kraftlog.entity.Exercise.EquipmentType.BARBELL)
                .muscleIds(List.of())
                .build();

        MvcResult result = mockMvc.perform(post("/api/exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exerciseRequest2)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID exerciseId2 = UUID.fromString(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        WorkoutExerciseController.WorkoutExerciseAddRequest request2 = 
                WorkoutExerciseController.WorkoutExerciseAddRequest.builder()
                        .workoutId(workoutId)
                        .exerciseId(exerciseId2)
                        .recommendedSets(4)
                        .recommendedReps(8)
                        .orderIndex(2)
                        .build();

        mockMvc.perform(post("/api/workout-exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/api/workout-exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .param("workoutId", workoutId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].exerciseName").value("Bench Press"))
                .andExpect(jsonPath("$[0].recommendedSets").value(3))
                .andExpect(jsonPath("$[1].exerciseName").value("Squat"))
                .andExpect(jsonPath("$[1].recommendedSets").value(4));
    }

    @Test
    @DisplayName("Should return empty array when workout has no exercises")
    void shouldReturnEmptyArrayWhenNoExercises() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/workout-exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .param("workoutId", workoutId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return 404 when getting exercises for non-existent workout")
    void shouldReturn404WhenGettingExercisesForNonExistentWorkout() throws Exception {
        // Given
        UUID nonExistentWorkoutId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/api/workout-exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .param("workoutId", nonExistentWorkoutId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should remove exercise from workout successfully")
    void shouldRemoveExerciseFromWorkout() throws Exception {
        // Given - add exercise first
        WorkoutExerciseController.WorkoutExerciseAddRequest addRequest = 
                WorkoutExerciseController.WorkoutExerciseAddRequest.builder()
                        .workoutId(workoutId)
                        .exerciseId(exerciseId)
                        .recommendedSets(3)
                        .recommendedReps(10)
                        .build();

        mockMvc.perform(post("/api/workout-exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isCreated());

        // When - remove exercise
        mockMvc.perform(delete("/api/workout-exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .param("workoutId", workoutId.toString())
                        .param("exerciseId", exerciseId.toString()))
                .andExpect(status().isNoContent());

        // Then - verify it's removed
        mockMvc.perform(get("/api/workout-exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .param("workoutId", workoutId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return 404 when removing exercise from non-existent workout")
    void shouldReturn404WhenRemovingFromNonExistentWorkout() throws Exception {
        // Given
        UUID nonExistentWorkoutId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete("/api/workout-exercises")
                        .header("Authorization", "Bearer " + authToken)
                        .param("workoutId", nonExistentWorkoutId.toString())
                        .param("exerciseId", exerciseId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when not authenticated")
    void shouldReturn403WhenNotAuthenticated() throws Exception {
        // Given
        WorkoutExerciseController.WorkoutExerciseAddRequest request = 
                WorkoutExerciseController.WorkoutExerciseAddRequest.builder()
                        .workoutId(workoutId)
                        .exerciseId(exerciseId)
                        .recommendedSets(3)
                        .recommendedReps(10)
                        .build();

        // When & Then
        mockMvc.perform(post("/api/workout-exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
