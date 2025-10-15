package com.kraftlog.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraftlog.dto.*;
import com.kraftlog.entity.Exercise;
import com.kraftlog.entity.User;
import com.kraftlog.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
public class WorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MuscleRepository muscleRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private LogRoutineRepository logRoutineRepository;

    @Autowired
    private LogSetRepository logSetRepository;

    private String jwtToken;
    private UUID userId;
    private UUID exerciseId;
    private UUID routineId;
    private UUID logRoutineId;
    private UUID logWorkoutId;
    private UUID logExerciseId;

    @BeforeEach
    @Transactional
    public void cleanup() {
        logSetRepository.deleteAll();
        logRoutineRepository.deleteAll();
        routineRepository.deleteAll();
        exerciseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    public void testCompleteAuthenticationWorkflow() throws Exception {
        // 1. Register a new user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .password("securePassword123")
                .birthDate(LocalDate.of(1990, 1, 15))
                .weightKg(80.0)
                .heightCm(180.0)
                .fitnessGoal(User.FitnessGoal.HYPERTROPHY)
                .build();

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
                .andReturn();

        LoginResponse registerResponse = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(), LoginResponse.class);

        assertThat(registerResponse.getToken()).isNotEmpty();
        this.userId = registerResponse.getUser().getId();
        this.jwtToken = registerResponse.getToken();

        // 2. Login with the registered user
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john.doe@example.com")
                .password("securePassword123")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), LoginResponse.class);

        assertThat(loginResponse.getToken()).isNotEmpty();

        // 3. Access protected endpoint without token - should fail
        mockMvc.perform(get("/api/exercises"))
                .andExpect(status().isForbidden());

        // 4. Access protected endpoint with token - should succeed
        mockMvc.perform(get("/api/exercises")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    public void testCompleteExerciseWorkflow() throws Exception {
        // Setup: Create user and get token
        setupAuthenticatedUser();

        // 1. Create a test muscle (since Flyway is disabled in tests)
        com.kraftlog.entity.Muscle testMuscle = com.kraftlog.entity.Muscle.builder()
                .name("Test Muscle")
                .muscleGroup(com.kraftlog.entity.Muscle.MuscleGroup.CHEST)
                .build();
        testMuscle = muscleRepository.save(testMuscle);
        UUID muscleId = testMuscle.getId();

        // 2. Create a new exercise
        ExerciseCreateRequest createRequest = ExerciseCreateRequest.builder()
                .name("Barbell Bench Press")
                .description("Classic chest exercise")
                .sets(4)
                .repetitions(10)
                .defaultWeightKg(80.0)
                .equipmentType(Exercise.EquipmentType.BARBELL)
                .muscleIds(List.of(muscleId))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/exercises")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Barbell Bench Press"))
                .andExpect(jsonPath("$.sets").value(4))
                .andExpect(jsonPath("$.equipmentType").value("BARBELL"))
                .andReturn();

        ExerciseResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ExerciseResponse.class);
        this.exerciseId = created.getId();

        // 3. Get exercise by ID
        mockMvc.perform(get("/api/exercises/" + exerciseId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(exerciseId.toString()))
                .andExpect(jsonPath("$.name").value("Barbell Bench Press"));

        // 4. Update exercise
        ExerciseUpdateRequest updateRequest = ExerciseUpdateRequest.builder()
                .sets(5)
                .repetitions(8)
                .build();

        mockMvc.perform(put("/api/exercises/" + exerciseId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sets").value(5))
                .andExpect(jsonPath("$.repetitions").value(8));

        // 5. Get all exercises
        mockMvc.perform(get("/api/exercises")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Barbell Bench Press"));
    }

    @Test
    @Order(3)
    public void testCompleteRoutineWorkflow() throws Exception {
        // Setup: Create user and get token
        setupAuthenticatedUser();

        // 1. Create a routine
        RoutineCreateRequest createRequest = RoutineCreateRequest.builder()
                .name("Chest Day")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .isActive(true)
                .userId(userId)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/routines")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Chest Day"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn();

        RoutineResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), RoutineResponse.class);
        this.routineId = created.getId();

        // 2. Get routine by ID
        mockMvc.perform(get("/api/routines/" + routineId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(routineId.toString()))
                .andExpect(jsonPath("$.name").value("Chest Day"));

        // 3. Get routines by user ID
        mockMvc.perform(get("/api/routines/user/" + userId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Chest Day"));

        // 4. Update routine
        RoutineCreateRequest updateRequest = RoutineCreateRequest.builder()
                .name("Upper Body Day")
                .isActive(false)
                .userId(userId)
                .build();

        mockMvc.perform(put("/api/routines/" + routineId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Upper Body Day"))
                .andExpect(jsonPath("$.isActive").value(false));

        // 5. Get all routines
        mockMvc.perform(get("/api/routines")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    public void testCompleteWorkoutLoggingWorkflow() throws Exception {
        // Setup: Create user, exercise, and routine
        setupAuthenticatedUser();
        createTestExercise();
        createTestRoutine();

        // 1. Start a routine session (Log Routine)
        LogRoutineCreateRequest logRoutineRequest = LogRoutineCreateRequest.builder()
                .routineId(routineId)
                .startDatetime(LocalDateTime.now())
                .build();

        MvcResult logRoutineResult = mockMvc.perform(post("/api/log-routines")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logRoutineRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.routineId").value(routineId.toString()))
                .andExpect(jsonPath("$.startDatetime").exists())
                .andReturn();

        LogRoutineResponse logRoutine = objectMapper.readValue(
                logRoutineResult.getResponse().getContentAsString(), LogRoutineResponse.class);
        this.logRoutineId = logRoutine.getId();

        // 2. Get log routine by ID
        mockMvc.perform(get("/api/log-routines/" + logRoutineId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(logRoutineId.toString()));

        // 3. Update log routine (set end time)
        LogRoutineCreateRequest updateLogRoutineRequest = LogRoutineCreateRequest.builder()
                .routineId(routineId)
                .startDatetime(LocalDateTime.now().minusHours(1))
                .endDatetime(LocalDateTime.now())
                .build();

        mockMvc.perform(put("/api/log-routines/" + logRoutineId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateLogRoutineRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endDatetime").exists());

        // 4. Get all log routines
        mockMvc.perform(get("/api/log-routines")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(5)
    public void testCompleteEndToEndWorkflow() throws Exception {
        // This test demonstrates the complete workflow from registration to logging sets

        // 1. Register and authenticate
        setupAuthenticatedUser();

        // 2. Get muscles
        List<UUID> muscleIds = getMuscleIds();

        // 3. Create exercise
        exerciseId = createExercise(muscleIds.get(0));

        // 4. Create routine
        routineId = createRoutine();

        // 5. Start workout session
        logRoutineId = startWorkoutSession();

        // 6. Verify all data was created correctly
        assertThat(userId).isNotNull();
        assertThat(exerciseId).isNotNull();
        assertThat(routineId).isNotNull();
        assertThat(logRoutineId).isNotNull();

        // 7. Verify data can be retrieved
        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/exercises/" + exerciseId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/routines/" + routineId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/log-routines/" + logRoutineId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    // Helper methods

    private void setupAuthenticatedUser() throws Exception {
        // Create an admin user for tests that need to create exercises
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Test")
                .surname("User")
                .email("test" + System.currentTimeMillis() + "@example.com")
                .password("password123")
                .birthDate(LocalDate.of(1990, 1, 1))
                .weightKg(75.0)
                .heightCm(175.0)
                .fitnessGoal(User.FitnessGoal.HYPERTROPHY)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponse.class);

        this.jwtToken = response.getToken();
        this.userId = response.getUser().getId();

        // Make the user an admin for exercise creation
        com.kraftlog.entity.User user = userRepository.findById(userId).orElseThrow();
        user = com.kraftlog.entity.User.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .password(user.getPassword())
                .birthDate(user.getBirthDate())
                .weightKg(user.getWeightKg())
                .heightCm(user.getHeightCm())
                .fitnessGoal(user.getFitnessGoal())
                .isAdmin(true)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .routines(user.getRoutines())
                .build();
        userRepository.save(user);

        // Re-login to get updated token with admin role
        LoginRequest loginRequest = LoginRequest.builder()
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), LoginResponse.class);
        this.jwtToken = loginResponse.getToken();
    }

    private void createTestExercise() throws Exception {
        List<UUID> muscleIds = getMuscleIds();
        this.exerciseId = createExercise(muscleIds.get(0));
    }

    private void createTestRoutine() throws Exception {
        this.routineId = createRoutine();
    }

    private List<UUID> getMuscleIds() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/muscles")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        List<MuscleResponse> muscles = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, MuscleResponse.class));

        // If no muscles exist (because Flyway is disabled in tests), create a test muscle
        if (muscles.isEmpty()) {
            com.kraftlog.entity.Muscle testMuscle = com.kraftlog.entity.Muscle.builder()
                    .name("Test Muscle")
                    .muscleGroup(com.kraftlog.entity.Muscle.MuscleGroup.CHEST)
                    .build();
            testMuscle = muscleRepository.save(testMuscle);
            return List.of(testMuscle.getId());
        }

        return muscles.stream().map(MuscleResponse::getId).toList();
    }

    private UUID createExercise(UUID muscleId) throws Exception {
        ExerciseCreateRequest request = ExerciseCreateRequest.builder()
                .name("Test Exercise")
                .sets(3)
                .repetitions(10)
                .defaultWeightKg(50.0)
                .equipmentType(Exercise.EquipmentType.BARBELL)
                .muscleIds(List.of(muscleId))
                .build();

        MvcResult result = mockMvc.perform(post("/api/exercises")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        ExerciseResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ExerciseResponse.class);

        return response.getId();
    }

    private UUID createRoutine() throws Exception {
        RoutineCreateRequest request = RoutineCreateRequest.builder()
                .name("Test Routine")
                .startDate(LocalDate.now())
                .isActive(true)
                .userId(userId)
                .build();

        MvcResult result = mockMvc.perform(post("/api/routines")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        RoutineResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), RoutineResponse.class);

        return response.getId();
    }

    private UUID startWorkoutSession() throws Exception {
        LogRoutineCreateRequest request = LogRoutineCreateRequest.builder()
                .routineId(routineId)
                .startDatetime(LocalDateTime.now())
                .build();

        MvcResult result = mockMvc.perform(post("/api/log-routines")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        LogRoutineResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), LogRoutineResponse.class);

        return response.getId();
    }
}
