package com.kraftlog.service;

import com.kraftlog.dto.ExerciseCreateRequest;
import com.kraftlog.dto.ExerciseResponse;
import com.kraftlog.dto.ExerciseUpdateRequest;
import com.kraftlog.dto.MuscleResponse;
import com.kraftlog.entity.Exercise;
import com.kraftlog.entity.Muscle;
import com.kraftlog.exception.BadRequestException;
import com.kraftlog.exception.ResourceNotFoundException;
import com.kraftlog.repository.ExerciseRepository;
import com.kraftlog.repository.MuscleRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final MuscleRepository muscleRepository;
    private final ModelMapper modelMapper;

    public ExerciseResponse createExercise(ExerciseCreateRequest request) {
        Exercise exercise = modelMapper.map(request, Exercise.class);

        if (request.getMuscleIds() != null && !request.getMuscleIds().isEmpty()) {
            List<Muscle> muscles = muscleRepository.findAllById(request.getMuscleIds());
            if (muscles.size() != request.getMuscleIds().size()) {
                throw new BadRequestException("One or more muscle IDs are invalid");
            }
            exercise.setMuscles(muscles);
        }

        Exercise savedExercise = exerciseRepository.save(exercise);
        return mapToResponse(savedExercise);
    }

    @Transactional(readOnly = true)
    public ExerciseResponse getExerciseById(UUID id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", id));
        return mapToResponse(exercise);
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExerciseResponse updateExercise(UUID id, ExerciseUpdateRequest request) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", id));

        if (request.getName() != null) {
            exercise.setName(request.getName());
        }
        if (request.getDescription() != null) {
            exercise.setDescription(request.getDescription());
        }
        if (request.getSets() != null) {
            exercise.setSets(request.getSets());
        }
        if (request.getRepetitions() != null) {
            exercise.setRepetitions(request.getRepetitions());
        }
        if (request.getTechnique() != null) {
            exercise.setTechnique(request.getTechnique());
        }
        if (request.getDefaultWeightKg() != null) {
            exercise.setDefaultWeightKg(request.getDefaultWeightKg());
        }
        if (request.getEquipmentType() != null) {
            exercise.setEquipmentType(request.getEquipmentType());
        }
        if (request.getMuscleIds() != null) {
            List<Muscle> muscles = muscleRepository.findAllById(request.getMuscleIds());
            if (muscles.size() != request.getMuscleIds().size()) {
                throw new BadRequestException("One or more muscle IDs are invalid");
            }
            exercise.setMuscles(muscles);
        }

        Exercise updatedExercise = exerciseRepository.save(exercise);
        return mapToResponse(updatedExercise);
    }

    public void deleteExercise(UUID id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", id));
        exerciseRepository.delete(exercise);
    }

    private ExerciseResponse mapToResponse(Exercise exercise) {
        ExerciseResponse response = modelMapper.map(exercise, ExerciseResponse.class);
        if (exercise.getMuscles() != null) {
            List<MuscleResponse> muscleResponses = exercise.getMuscles().stream()
                    .map(muscle -> modelMapper.map(muscle, MuscleResponse.class))
                    .collect(Collectors.toList());
            response.setMuscles(muscleResponses);
        }
        return response;
    }
}