package com.kraftlog.service;

import com.kraftlog.dto.ChangePasswordRequest;
import com.kraftlog.dto.UserResponse;
import com.kraftlog.entity.User;
import com.kraftlog.exception.ResourceNotFoundException;
import com.kraftlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        log.info("Admin deleting user with email: {}", user.getEmail());
        userRepository.delete(user);
    }

    public UserResponse changeUserPassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);

        User updatedUser = userRepository.save(user);
        log.info("Admin changed password for user with email: {}", user.getEmail());

        return modelMapper.map(updatedUser, UserResponse.class);
    }
}