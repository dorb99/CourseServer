package com.courseServer.security;

import com.courseServer.enteties.User;
import com.courseServer.enteties.UserDto;
import com.courseServer.repository.UserRepo;
import com.courseServer.services.SecurityService;
import com.courseServer.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(com.courseServer.config.TestConfig.class)
@ActiveProfiles("test")
public class SecurityTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        userRepo.deleteAll();

        // Create a test user
        UserDto userDto = new UserDto("testuser", 25, "testpassword");
        testUser = userService.create(userDto);

        // Login to get JWT token
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "testpassword");

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
            "/api/v1/auth/login",
            loginRequest,
            Map.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        assertTrue(loginResponse.getBody().containsKey("token"));
        
        jwtToken = (String) loginResponse.getBody().get("token");
    }

    @Test
    void testLoginWithValidCredentials() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "testpassword");

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/v1/auth/login",
            loginRequest,
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("token"));
    }

    @Test
    void testLoginWithInvalidCredentials() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "wrongpassword");

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/v1/auth/login",
            loginRequest,
            Map.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testAccessProtectedEndpointWithoutToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/v1/users",
            String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testAccessProtectedEndpointWithValidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/users",
            HttpMethod.GET,
            entity,
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAccessProtectedEndpointWithInvalidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.token.here");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/users",
            HttpMethod.GET,
            entity,
            String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testPasswordHashing() {
        String rawPassword = "testpassword";
        String hashedPassword = testUser.getPassword();

        assertTrue(passwordEncoder.matches(rawPassword, hashedPassword));
        assertNotEquals(rawPassword, hashedPassword);
    }

    @Test
    void testPublicEndpointsAccessibleWithoutToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/",
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCreateUserWithPasswordHashing() {
        UserDto newUserDto = new UserDto("newuser", 30, "newpassword");
        User createdUser = userService.create(newUserDto);

        assertNotNull(createdUser);
        assertNotNull(createdUser.getPassword());
        assertNotEquals("newpassword", createdUser.getPassword());
        assertTrue(passwordEncoder.matches("newpassword", createdUser.getPassword()));
    }

    @Test
    void testUpdateUserPasswordHashing() {
        String newPassword = "updatedpassword";
        Map<String, Object> updates = new HashMap<>();
        updates.put("password", newPassword);

        User updatedUser = userService.patchUser(testUser.getId(), updates);

        assertNotNull(updatedUser);
        assertNotEquals(newPassword, updatedUser.getPassword());
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }
} 