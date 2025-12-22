package com.kraftlog.service;

import com.kraftlog.dto.LogRoutineCreateRequest;
import com.kraftlog.dto.LogRoutineResponse;
import com.kraftlog.dto.LogWorkoutResponse;
import com.kraftlog.entity.LogRoutine;
import com.kraftlog.entity.Routine;
import com.kraftlog.exception.ResourceNotFoundException;
import com.kraftlog.repository.LogRoutineRepository;
import com.kraftlog.repository.RoutineRepository;
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
public class LogRoutineService {

    private final LogRoutineRepository logRoutineRepository;
    private final RoutineRepository routineRepository;
    private final ModelMapper modelMapper;

    public LogRoutineResponse createLogRoutine(LogRoutineCreateRequest request) {
        Routine routine = routineRepository.findById(request.getRoutineId())
                .orElseThrow(() -> new ResourceNotFoundException("Routine", "id", request.getRoutineId()));

        LogRoutine logRoutine = LogRoutine.builder()
                .routine(routine)
                .startDatetime(request.getStartDatetime())
                .endDatetime(request.getEndDatetime())
                .build();

        LogRoutine savedLogRoutine = logRoutineRepository.save(logRoutine);
        return mapToResponse(savedLogRoutine);
    }

    @Transactional(readOnly = true)
    public LogRoutineResponse getLogRoutineById(UUID id) {
        LogRoutine logRoutine = logRoutineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LogRoutine", "id", id));
        return mapToResponse(logRoutine);
    }

    @Transactional(readOnly = true)
    public List<LogRoutineResponse> getAllLogRoutines() {
        return logRoutineRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LogRoutineResponse> getLogRoutinesByUserId(UUID userId) {
        return logRoutineRepository.findByRoutine_UserIdOrderByStartDatetimeDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LogRoutineResponse> getLogRoutinesByRoutineId(UUID routineId) {
        List<LogRoutine> logRoutines = logRoutineRepository.findByRoutineIdOrderByStartDatetimeDesc(routineId);
        // Initialize lazy collections within transaction
        logRoutines.forEach(lr -> {
            lr.getLogWorkouts().size(); // Force initialization
            lr.getLogWorkouts().forEach(lw -> {
                lw.getLogExercises().size();
                lw.getLogExercises().forEach(le -> {
                    le.getLogSets().size();
                });
            });
        });
        return logRoutines.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LogRoutineResponse updateLogRoutine(UUID id, LogRoutineCreateRequest request) {
        LogRoutine logRoutine = logRoutineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LogRoutine", "id", id));

        if (request.getEndDatetime() != null) {
            logRoutine.setEndDatetime(request.getEndDatetime());
        }

        LogRoutine updatedLogRoutine = logRoutineRepository.save(logRoutine);
        return mapToResponse(updatedLogRoutine);
    }

    public void deleteLogRoutine(UUID id) {
        LogRoutine logRoutine = logRoutineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LogRoutine", "id", id));
        logRoutineRepository.delete(logRoutine);
    }

    private LogRoutineResponse mapToResponse(LogRoutine logRoutine) {
        LogRoutineResponse response = modelMapper.map(logRoutine, LogRoutineResponse.class);
        response.setRoutineId(logRoutine.getRoutine().getId());
        // Explicitly map logWorkouts to ensure they're included
        response.setLogWorkouts(logRoutine.getLogWorkouts().stream()
                .map(lw -> modelMapper.map(lw, LogWorkoutResponse.class))
                .collect(Collectors.toList()));
        return response;
    }
}