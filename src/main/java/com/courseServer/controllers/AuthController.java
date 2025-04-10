package com.courseServer.controllers;

import com.courseServer.services.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor // Automatically creates constructor for final fields (AuthService)
public class AuthController {

    private final AuthService authService;

    // Simple DTO for login request payload
    @Data // Lombok annotation for getters, setters, toString, equals, hashCode
    static class LoginRequest {
        private String username;
        private String password;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String token = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            // Simple response containing just the token
            java.util.Map<String, String> responseBody = new java.util.HashMap<>();
            responseBody.put("token", token);
            return ResponseEntity.ok(responseBody);
        } catch (RuntimeException e) {
            // Return unauthorized if login fails
            java.util.Map<String, String> errorBody = new java.util.HashMap<>();
            errorBody.put("error", e.getMessage());
            return ResponseEntity.status(401).body(errorBody);
        }
    }

    // Optional: Add a /register endpoint here if you want separate registration
    // It would likely call userService.create after validating the input DTO
    // Example:
    /*
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDto registrationDto) {
        try {
            // Optionally check if user already exists
            if (userService.findByName(registrationDto.getName()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
            }
            User createdUser = userService.create(registrationDto);
            // Return a success message or the created user DTO (without password)
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
        } catch (Exception e) {
            // Log error
            return ResponseEntity.internalServerError().body(Map.of("error", "Registration failed"));
        }
    }
    */
} 