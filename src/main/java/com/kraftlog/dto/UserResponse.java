package com.kraftlog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String name;
    private String surname;
    
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate birthDate;
    
    private String email;
    private Double weightKg;
    private Double heightCm;
    
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updatedAt;
}
