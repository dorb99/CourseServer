package com.courseServer.controllers;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.courseServer.entities.User;
import com.courseServer.entities.UserDto;
import com.courseServer.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
	
	private final UserService service;
	
	public UserController(UserService service) {
		this.service = service;
	}
	
	// --- Helper method for User -> UserDto conversion ---
	// In a real app, use a dedicated Mapper class (e.g., using MapStruct)
	private UserDto convertToDto(User user) {
		// Use the constructor that EXCLUDES the password for responses
		return new UserDto(user.getName(), user.getAge());
	}
	// --------------------------------------------------

	// Get all users
	@GetMapping()
	public ResponseEntity<List<UserDto>> getAll() {
		List<User> users = service.getAll();
		List<UserDto> userDtos = users.stream()
									  .map(this::convertToDto)
									  .collect(Collectors.toList());
		return ResponseEntity.ok(userDtos);
	}

	// Get a single user by ID
	@GetMapping("/{id}")
	public ResponseEntity<UserDto> getOne(@PathVariable(name = "id") Long id) {
		User user = service.getOne(id);
		return ResponseEntity.ok(convertToDto(user));
	}
	
	// Create a new user
	@PostMapping()
	public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userDto) {
		User createdUser = service.create(userDto);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
									  .path("/{id}")
									  .buildAndExpand(createdUser.getId())
									  .toUri();

		return ResponseEntity.created(location).body(convertToDto(createdUser));
	}

/*
	// --- Alternative POST using org.json --- 
	// Note: This approach bypasses Spring's validation and DTO mapping. 
	// It's generally less robust and not recommended.
	@PostMapping("/alternative")
	public ResponseEntity<?> createAlternative(@RequestBody String jsonBody) {
	    try {
	        org.json.JSONObject jsonObject = new org.json.JSONObject(jsonBody);

	        // Manual extraction and type checking
	        String name = jsonObject.optString("name", null);
	        Integer age = jsonObject.has("age") ? jsonObject.optInt("age") : null;

	        // Manual validation (example)
	        if (name == null || name.isBlank()) {
	            // Return a 400 Bad Request manually
	            return ResponseEntity.badRequest().body("{\"error\": \"Name is required\"}"); 
	        }
	        if (age == null || age < 0) {
	            // Return a 400 Bad Request manually
	            return ResponseEntity.badRequest().body("{\"error\": \"Valid positive age is required\"}");
	        }

	        // Manually create DTO (or pass values directly to service if it accepts them)
	        UserDto userDto = new UserDto(name, age);
	        User createdUser = service.create(userDto);

	        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
	                                                  .path("/{id}")
	                                                  .buildAndExpand(createdUser.getId())
	                                                  .toUri();
	        
	        // Manually create response DTO
	        UserDto responseDto = convertToDto(createdUser);
	        return ResponseEntity.created(location).body(responseDto);

	    } catch (org.json.JSONException e) {
	        // Handle invalid JSON format
	        return ResponseEntity.badRequest().body("{\"error\": \"Invalid JSON format\"}");
	    } catch (Exception e) {
	        // Generic error handling (should ideally use @ExceptionHandler)
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"An internal error occurred\"}");
	    }
	}
*/

	// Update an existing user (PUT - Replace)
	// PUT should replace the entire resource state.
	@PutMapping("/{id}")
	public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) { // Use @Valid
		// Service method handles update logic and throws UserNotFoundException
		User updatedUser = service.updateUser(id, userDto);
		return ResponseEntity.ok(convertToDto(updatedUser)); // Return 200 OK with updated DTO
	}

/*
	// --- Alternative PUT using org.json --- 
	// Note: Similar drawbacks as the alternative POST (manual handling, less robust).
	@PutMapping("/alternative/{id}")
	public ResponseEntity<?> updateAlternative(@PathVariable Long id, @RequestBody String jsonBody) {
	    try {
	        org.json.JSONObject jsonObject = new org.json.JSONObject(jsonBody);

	        String name = jsonObject.optString("name", null);
	        Integer age = jsonObject.has("age") ? jsonObject.optInt("age") : null;

	        // Manual validation (ensure required fields for PUT are present)
	        if (name == null || name.isBlank() || age == null || age < 0) {
	             return ResponseEntity.badRequest().body("{\"error\": \"Both name and valid positive age are required for PUT\"}");
	        }

	        UserDto userDto = new UserDto(name, age);

	        // Call the existing service method (it handles UserNotFoundException)
	        User updatedUser = service.updateUser(id, userDto);
	        return ResponseEntity.ok(convertToDto(updatedUser));

	    } catch (org.json.JSONException e) {
	        return ResponseEntity.badRequest().body("{\"error\": \"Invalid JSON format\"}");
	    } catch (UserNotFoundException e) { // Catch specific exception from service
             // Example of manual exception handling (GlobalExceptionHandler is preferred)
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"An internal error occurred\"}");
	    }
	}
*/

	// Partially update an existing user (PATCH)
	// PATCH applies partial modifications to a resource.
	@PatchMapping("/{id}")
	public ResponseEntity<UserDto> patchUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
		User patchedUser = service.patchUser(id, updates);
		return ResponseEntity.ok(convertToDto(patchedUser));
	}

	// Delete a user by ID
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}