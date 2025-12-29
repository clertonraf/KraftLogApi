package com.kraftlog.service;

public interface IEmailService {
    void sendPasswordResetEmail(String toEmail, String token);
}
