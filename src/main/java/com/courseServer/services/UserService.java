package com.courseServer.services;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import com.courseServer.enteties.User;
import com.courseServer.enteties.UserDto;
import com.courseServer.exceptions.UserNotFoundException;
import com.courseServer.repository.UserRepo;

@Service
public class UserService {

	private final UserRepo repository;
	
	@Autowired
	public UserService(UserRepo repository) {
		this.repository = repository; // Assign the injected repository
	}
	
	// Get all
	public List<User> getAll() {
		return (List<User>) repository.findAll();
	}

	// Get one
	public User getOne(Long id) {
		// Find user or throw custom exception if not found
		return repository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
	}
	
	// Create
	public User create(UserDto userDto) {
		// Convert DTO to User entity
		User user = new User(userDto.getName(), userDto.getAge());
		// In a real app, consider using a dedicated Mapper (e.g., MapStruct)
		return repository.save(user); // Return the saved user with ID
	}

	// Update (PUT - Replace entire resource)
	public User updateUser(Long id, UserDto userDto) {
		// getOne will throw exception if user not found
		User existingUser = getOne(id);
		// Update entity fields from DTO
		existingUser.setName(userDto.getName());
		existingUser.setAge(userDto.getAge());
		return repository.save(existingUser); // Save and return updated user
	}

	// Update (PATCH - Partially update resource)
	// Note: Using Map<String, Object> is simple but less type-safe.
	// Consider JSON Patch (RFC 6902) or a dedicated Patch DTO for robustness.
	public User patchUser(Long id, Map<String, Object> updates) {
		// getOne will throw exception if user not found
		User existingUser = getOne(id);

		updates.forEach((key, value) -> {
			// Use reflection to find and set field dynamically
			Field field = ReflectionUtils.findField(User.class, key);
			if (field != null) {
                // Ensure the field is accessible (e.g., private fields)
                field.setAccessible(true);
				// Basic type handling (can be expanded for more types)
				Object convertedValue = value;
				if (field.getType() == int.class && value instanceof Number) {
                    convertedValue = ((Number) value).intValue();
                } else if (field.getType() == Long.class && value instanceof Number) {
					convertedValue = ((Number) value).longValue();
				} else if (field.getType() == String.class && value != null) {
                    convertedValue = String.valueOf(value);
                }
                // Set the field value on the existing user object
				ReflectionUtils.setField(field, existingUser, convertedValue);
			}
			// Optional: Log or throw an error if the key doesn't match a field
		});

		return repository.save(existingUser); // Save and return patched user
	}

	// Delete
	public void delete(Long id) {
		// Check existence before deleting to provide specific feedback
		if (!repository.existsById(id)) {
			throw new UserNotFoundException("User not found with id: " + id);
		}
		repository.deleteById(id);
	}
}
