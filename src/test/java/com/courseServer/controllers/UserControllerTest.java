package com.courseServer.controllers;

import com.courseServer.entities.User;
import com.courseServer.entities.UserDto;
import com.courseServer.exceptions.UserNotFoundException;
import com.courseServer.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;


    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getUserById_returnsUser_whenUserExists() throws Exception {
        User user = new User("Alice", 30);
        user.setId(1L);

        when(userService.getOne(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.age").value(30));
    }

    @Test
    void getUserById_returnsNotFound_whenUserDoesNotExist() throws Exception {
        when(userService.getOne(99L)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_returnsCreatedUser_whenValidInput() throws Exception {
        UserDto dto = new UserDto("Charlie", 40);
        User saved = new User("Charlie", 40);
        saved.setId(3L);

        when(userService.create(any(UserDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/users/3"))
                .andExpect(jsonPath("$.name").value("Charlie"))
                .andExpect(jsonPath("$.age").value(40));
    }

    @Test
    void getAllUsers_returnsListOfUsers() throws Exception {
        User user1 = new User("Alice", 30);
        user1.setId(1L);
        User user2 = new User("Bob", 25);
        user2.setId(2L);

        when(userService.getAll()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void patchUser_updatesFieldsCorrectly() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("age", 35);

        User updated = new User("Alice", 35);
        updated.setId(1L);

        when(userService.patchUser(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.age").value(35));
    }

    @Test
    void testGetUserById() {
        User user = new User("testuser", "password", "Test User", 25);
        when(userService.getUserById(1L)).thenReturn(user);

        UserDto result = userController.getUserById(1L);
        assertEquals("Test User", result.getName());
        assertEquals(25, result.getAge());
    }

    @Test
    void testCreateUser() {
        UserDto userDto = new UserDto(null, "New User", 30);
        User user = new User("newuser", "password", "New User", 30);
        when(userService.createUser(any(User.class))).thenReturn(user);

        ResponseEntity<UserDto> response = userController.createUser(userDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("New User", response.getBody().getName());
    }

    @Test
    void testUpdateUser() {
        UserDto userDto = new UserDto(1L, "Updated User", 35);
        User user = new User("updateduser", "password", "Updated User", 35);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(user);

        UserDto result = userController.updateUser(1L, userDto);
        assertEquals("Updated User", result.getName());
        assertEquals(35, result.getAge());
    }

    @Test
    void testPatchUser() {
        User user = new User("patcheduser", "password", "Patched User", 40);
        when(userService.patchUser(eq(1L), any(Map.class))).thenReturn(user);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Patched User");
        updates.put("age", 40);

        UserDto result = userController.patchUser(1L, updates);
        assertEquals("Patched User", result.getName());
        assertEquals(40, result.getAge());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userService).deleteUser(1L);
        userController.deleteUser(1L);
        verify(userService, times(1)).deleteUser(1L);
    }
}
