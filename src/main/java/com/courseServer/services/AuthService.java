package com.courseServer.services;

import com.courseServer.entities.User;
import com.courseServer.repositories.UserRepo;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepo userRepo;
    private final Map<String, String> activeSessions = new HashMap<>(); // token -> username

    public AuthService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public String login(String username, String password) {
        User user = userRepo.findByUsername(username);
        if (user == null || !verifyPassword(password, user.getPassword())) {
            return null;
        }

        String token = generateToken();
        activeSessions.put(token, username);
        return token;
    }

    public boolean validateToken(String token) {
        return token != null && activeSessions.containsKey(token);
    }

    public String getUsernameFromToken(String token) {
        return activeSessions.get(token);
    }

    public void logout(String token) {
        activeSessions.remove(token);
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private boolean verifyPassword(String inputPassword, String storedHash) {
        String inputHash = hashPassword(inputPassword);
        return inputHash.equals(storedHash);
    }
} 