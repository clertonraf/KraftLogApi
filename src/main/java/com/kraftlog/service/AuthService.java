package com.kraftlog.service;

import com.kraftlog.dto.LoginRequest;
import com.kraftlog.dto.LoginResponse;
import com.kraftlog.dto.RegisterRequest;
import com.kraftlog.dto.UserResponse;
import com.kraftlog.entity.User;
import com.kraftlog.exception.BadRequestException;
import com.kraftlog.repository.UserRepository;
import com.kraftlog.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final ModelMapper modelMapper;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        User user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .birthDate(request.getBirthDate())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .weightKg(request.getWeightKg())
                .heightCm(request.getHeightCm())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        // Map to response
        UserResponse userResponse = modelMapper.map(savedUser, UserResponse.class);

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userResponse)
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // Generate JWT token
        String token = jwtUtil.generateToken(userDetails);

        // Get user from database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Map to response
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userResponse)
                .build();
    }
}
