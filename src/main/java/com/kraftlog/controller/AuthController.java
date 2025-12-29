package com.kraftlog.controller;

import com.kraftlog.dto.*;
import com.kraftlog.service.AuthService;
import com.kraftlog.service.PasswordRecoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private final AuthService authService;
    private final PasswordRecoveryService passwordRecoveryService;

    @Operation(summary = "Register a new user", description = "Creates a new user account and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already in use", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Login user", description = "Authenticates user credentials and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Request password recovery", description = "Initiates password recovery process by sending an email with a reset link")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password recovery email sent successfully",
                    content = @Content(schema = @Schema(implementation = PasswordRecoveryResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found with provided email", content = @Content)
    })
    @PostMapping("/password-recovery")
    public ResponseEntity<PasswordRecoveryResponse> requestPasswordRecovery(@Valid @RequestBody PasswordRecoveryRequest request) {
        passwordRecoveryService.initiatePasswordRecovery(request.getEmail());
        PasswordRecoveryResponse response = PasswordRecoveryResponse.builder()
                .message("Password recovery email sent successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reset password", description = "Resets user password using a valid reset token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully",
                    content = @Content(schema = @Schema(implementation = PasswordResetResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content)
    })
    @PostMapping("/password-reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordRecoveryService.resetPassword(request.getToken(), request.getNewPassword());
        PasswordResetResponse response = PasswordResetResponse.builder()
                .message("Password has been reset successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
