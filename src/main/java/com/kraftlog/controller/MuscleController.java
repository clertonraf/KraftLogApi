package com.kraftlog.controller;

import com.kraftlog.dto.MuscleResponse;
import com.kraftlog.entity.Muscle;
import com.kraftlog.repository.MuscleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/muscles")
@RequiredArgsConstructor
@Tag(name = "Muscle Management", description = "APIs for viewing muscle groups")
public class MuscleController {

    private final MuscleRepository muscleRepository;
    private final ModelMapper modelMapper;

    @Operation(summary = "Get all muscles", description = "Returns all available muscles and muscle groups")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Muscles retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MuscleResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<MuscleResponse>> getAllMuscles() {
        List<MuscleResponse> muscles = muscleRepository.findAll().stream()
                .map(muscle -> modelMapper.map(muscle, MuscleResponse.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(muscles);
    }
}