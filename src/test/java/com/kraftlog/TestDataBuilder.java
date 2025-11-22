package com.kraftlog;

import com.kraftlog.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class TestDataBuilder {

    public static User.UserBuilder defaultUser() {
        return User.builder()
                .name("John")
                .surname("Doe")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("john.doe@example.com")
                .password("password123")
                .weightKg(75.5)
                .heightCm(180.0)
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .routines(new ArrayList<>());
    }

    public static User.UserBuilder adminUser() {
        return User.builder()
                .name("Admin")
                .surname("User")
                .birthDate(LocalDate.of(1985, 5, 15))
                .email("admin@kraftlog.com")
                .password("admin123")
                .isAdmin(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .routines(new ArrayList<>());
    }

    public static Muscle.MuscleBuilder defaultMuscle() {
        return Muscle.builder()
                .name("Pectoralis Major")
                .muscleGroup(Muscle.MuscleGroup.CHEST);
    }

    public static Exercise.ExerciseBuilder defaultExercise() {
        return Exercise.builder()
                .name("Bench Press")
                .description("Compound chest exercise")
                .sets(4)
                .repetitions(10)
                .technique("Lower bar to chest, press up")
                .defaultWeightKg(60.0)
                .equipmentType(Exercise.EquipmentType.BARBELL)
                .muscles(new ArrayList<>());
    }

    public static Routine.RoutineBuilder defaultRoutine(User user) {
        return Routine.builder()
                .name("Push Pull Legs")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .isActive(true)
                .user(user)
                .workouts(new ArrayList<>())
                .aerobicActivities(new ArrayList<>());
    }

    public static Workout.WorkoutBuilder defaultWorkout(Routine routine) {
        return Workout.builder()
                .name("Push Day")
                .orderIndex(1)
                .intervalMinutes(90)
                .routine(routine)
                .workoutExercises(new ArrayList<>())
                .muscles(new ArrayList<>());
    }

    public static AerobicActivity.AerobicActivityBuilder defaultAerobicActivity(Routine routine) {
        return AerobicActivity.builder()
                .name("Running")
                .durationMinutes(30)
                .notes("Light jog")
                .routine(routine);
    }

    public static LogRoutine.LogRoutineBuilder defaultLogRoutine(Routine routine) {
        return LogRoutine.builder()
                .routine(routine)
                .startDatetime(LocalDateTime.now())
                .endDatetime(null)
                .logWorkouts(new ArrayList<>());
    }

    public static LogWorkout.LogWorkoutBuilder defaultLogWorkout(LogRoutine logRoutine, Workout workout) {
        return LogWorkout.builder()
                .logRoutine(logRoutine)
                .workout(workout)
                .startDatetime(LocalDateTime.now())
                .endDatetime(null)
                .logExercises(new ArrayList<>());
    }

    public static LogExercise.LogExerciseBuilder defaultLogExercise(LogWorkout logWorkout, Exercise exercise) {
        return LogExercise.builder()
                .logWorkout(logWorkout)
                .exercise(exercise)
                .startDatetime(LocalDateTime.now())
                .endDatetime(null)
                .notes("Felt good")
                .repetitions(10)
                .completed(true)
                .logSets(new ArrayList<>());
    }

    public static LogSet.LogSetBuilder defaultLogSet(LogExercise logExercise) {
        return LogSet.builder()
                .logExercise(logExercise)
                .setNumber(1)
                .reps(10)
                .weightKg(60.0)
                .restTimeSeconds(90)
                .timestamp(LocalDateTime.now())
                .notes("Good form");
    }
}
