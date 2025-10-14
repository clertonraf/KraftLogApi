package com.kraftlog.service;

import com.kraftlog.dto.LogSetCreateRequest;
import com.kraftlog.dto.LogSetResponse;
import com.kraftlog.entity.LogExercise;
import com.kraftlog.entity.LogSet;
import com.kraftlog.exception.ResourceNotFoundException;
import com.kraftlog.repository.LogExerciseRepository;
import com.kraftlog.repository.LogSetRepository;
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
public class LogSetService {

    private final LogSetRepository logSetRepository;
    private final LogExerciseRepository logExerciseRepository;
    private final ModelMapper modelMapper;

    public LogSetResponse createLogSet(LogSetCreateRequest request) {
        LogExercise logExercise = logExerciseRepository.findById(request.getLogExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("LogExercise", "id", request.getLogExerciseId()));

        LogSet logSet = LogSet.builder()
                .logExercise(logExercise)
                .setNumber(request.getSetNumber())
                .reps(request.getReps())
                .weightKg(request.getWeightKg())
                .restTimeSeconds(request.getRestTimeSeconds())
                .timestamp(request.getTimestamp())
                .notes(request.getNotes())
                .build();

        LogSet savedLogSet = logSetRepository.save(logSet);
        return mapToResponse(savedLogSet);
    }

    @Transactional(readOnly = true)
    public LogSetResponse getLogSetById(UUID id) {
        LogSet logSet = logSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LogSet", "id", id));
        return mapToResponse(logSet);
    }

    @Transactional(readOnly = true)
    public List<LogSetResponse> getAllLogSets() {
        return logSetRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LogSetResponse> getLogSetsByLogExerciseId(UUID logExerciseId) {
        if (!logExerciseRepository.existsById(logExerciseId)) {
            throw new ResourceNotFoundException("LogExercise", "id", logExerciseId);
        }
        return logSetRepository.findByLogExerciseIdOrderBySetNumberAsc(logExerciseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LogSetResponse updateLogSet(UUID id, LogSetCreateRequest request) {
        LogSet logSet = logSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LogSet", "id", id));

        if (request.getSetNumber() != null) {
            logSet.setSetNumber(request.getSetNumber());
        }
        if (request.getReps() != null) {
            logSet.setReps(request.getReps());
        }
        if (request.getWeightKg() != null) {
            logSet.setWeightKg(request.getWeightKg());
        }
        if (request.getRestTimeSeconds() != null) {
            logSet.setRestTimeSeconds(request.getRestTimeSeconds());
        }
        if (request.getTimestamp() != null) {
            logSet.setTimestamp(request.getTimestamp());
        }
        if (request.getNotes() != null) {
            logSet.setNotes(request.getNotes());
        }

        LogSet updatedLogSet = logSetRepository.save(logSet);
        return mapToResponse(updatedLogSet);
    }

    public void deleteLogSet(UUID id) {
        LogSet logSet = logSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LogSet", "id", id));
        logSetRepository.delete(logSet);
    }

    private LogSetResponse mapToResponse(LogSet logSet) {
        LogSetResponse response = modelMapper.map(logSet, LogSetResponse.class);
        response.setLogExerciseId(logSet.getLogExercise().getId());
        return response;
    }
}
