package com.kraftlog.service;

import com.kraftlog.dto.RoutineCreateRequest;
import com.kraftlog.dto.RoutineResponse;
import com.kraftlog.entity.Routine;
import com.kraftlog.entity.User;
import com.kraftlog.exception.ResourceNotFoundException;
import com.kraftlog.repository.RoutineRepository;
import com.kraftlog.repository.UserRepository;
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
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public RoutineResponse createRoutine(RoutineCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Routine routine = Routine.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : false)
                .user(user)
                .build();

        Routine savedRoutine = routineRepository.save(routine);
        return mapToResponse(savedRoutine);
    }

    @Transactional(readOnly = true)
    public RoutineResponse getRoutineById(UUID id) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routine", "id", id));
        return mapToResponse(routine);
    }

    @Transactional(readOnly = true)
    public List<RoutineResponse> getAllRoutines() {
        return routineRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoutineResponse> getRoutinesByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return routineRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public RoutineResponse updateRoutine(UUID id, RoutineCreateRequest request) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routine", "id", id));

        if (request.getName() != null) {
            routine.setName(request.getName());
        }
        if (request.getStartDate() != null) {
            routine.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            routine.setEndDate(request.getEndDate());
        }
        if (request.getIsActive() != null) {
            routine.setIsActive(request.getIsActive());
        }

        Routine updatedRoutine = routineRepository.save(routine);
        return mapToResponse(updatedRoutine);
    }

    public void deleteRoutine(UUID id) {
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routine", "id", id));
        routineRepository.delete(routine);
    }

    private RoutineResponse mapToResponse(Routine routine) {
        RoutineResponse response = modelMapper.map(routine, RoutineResponse.class);
        response.setUserId(routine.getUser().getId());
        return response;
    }
}