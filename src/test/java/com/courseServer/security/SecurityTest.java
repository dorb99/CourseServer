package com.courseServer.security;

import com.courseServer.config.SecurityConfig;
import com.courseServer.controllers.AuthController;
import com.courseServer.controllers.UserController;
import com.courseServer.entities.User;
import com.courseServer.repositories.UserRepo;
import com.courseServer.services.AuthService;
import com.courseServer.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String validToken;
    private final String testUsername = "testuser";
    private final String testPassword = "testpass";

    @BeforeEach
    void setup() throws Exception {
        // Clear any existing test data
        userRepo.deleteAll();

        // Create a test user
        User testUser = new User();
        testUser.setUsername(testUsername);
        testUser.setPassword(passwordEncoder.encode(testPassword));
        userRepo.save(testUser);

        // Get a valid token for testing
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + testUsername + "\",\"password\":\"" + testPassword + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        validToken = response.substring(response.indexOf(":") + 2, response.length() - 2);
    }

    @Test
    void testPublicEndpointsAccessibleWithoutToken() throws Exception {
        // Test login endpoint
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + testUsername + "\",\"password\":\"" + testPassword + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        // Test static resources
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpointsRequireToken() throws Exception {
        // Try to access protected endpoint without token
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());

        // Try to access protected endpoint with invalid token
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isUnauthorized());

        // Try to access protected endpoint with valid token
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidCredentials() throws Exception {
        // Test login with wrong password
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + testUsername + "\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isUnauthorized());

        // Test login with non-existent user
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"nonexistent\",\"password\":\"anypass\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testTokenRequiredForAllProtectedOperations() throws Exception {
        // Test create user without token
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"newuser\",\"age\":25,\"password\":\"pass123\"}"))
                .andExpect(status().isUnauthorized());

        // Test update user without token
        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"updated\",\"age\":30}"))
                .andExpect(status().isUnauthorized());

        // Test delete user without token
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isUnauthorized());
    }
} 