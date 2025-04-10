package com.courseServer.services;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import com.courseServer.entities.User;
import com.courseServer.entities.UserDto;
import com.courseServer.exceptions.UserNotFoundException;
import com.courseServer.repositories.UserRepo;

@Service
public class UserService {

	private final UserRepo repository;
	private final SecurityService securityService;
	
	@Autowired
	public UserService(UserRepo repository, SecurityService securityService) {
		this.repository = repository;
		this.securityService = securityService;
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
	
	// Find user by name (needed for login)
	// Consider if username should be unique in a real application
	public Optional<User> findByName(String name) {
		// This assumes UserRepo has a method like findByName(String name)
		// If not, you'll need to add it to the UserRepo interface.
		return repository.findByName(name);
	}

	// Create user with password hashing
	public User create(UserDto userDto) {
		// Hash the password before saving
		String hashedPassword = securityService.hashPassword(userDto.getPassword());

		// Convert DTO to User entity, now including the *hashed* password
		User user = new User(userDto.getName(), userDto.getAge(), hashedPassword);

		// Consider adding logic to check if user with the same name already exists

		return repository.save(user); // Save the user with the hashed password
	}

	// Update (PUT - Replace entire resource)
	public User updateUser(Long id, UserDto userDto) {
		// getOne will throw exception if user not found
		User existingUser = getOne(id);
		// Update entity fields from DTO
		existingUser.setName(userDto.getName());
		existingUser.setAge(userDto.getAge());

		// Optionally handle password update via PUT (ensure DTO includes password)
		if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
		    String hashedNewPassword = securityService.hashPassword(userDto.getPassword());
		    existingUser.setPassword(hashedNewPassword);
		}

		return repository.save(existingUser); // Save and return updated user
	}

	// Update (PATCH - Partially update resource)
	// Note: Using Map<String, Object> is simple but less type-safe.
	// Consider JSON Patch (RFC 6902) or a dedicated Patch DTO for robustness.
	public User patchUser(Long id, Map<String, Object> updates) {
		// getOne will throw exception if user not found
		User existingUser = getOne(id);

		updates.forEach((key, value) -> {
			if ("password".equals(key) && value instanceof String) {
				// Hash the password if it's being patched
				 String hashedPassword = securityService.hashPassword((String) value);
				 existingUser.setPassword(hashedPassword);
			} else {
				Field field = ReflectionUtils.findField(User.class, key);
				if (field != null && !key.equals("id")) { // Exclude ID and already handled password
					field.setAccessible(true);
					Object convertedValue = value;
					if (field.getType() == int.class && value instanceof Number) {
						convertedValue = ((Number) value).intValue();
					} else if (field.getType() == Long.class && value instanceof Number) {
						convertedValue = ((Number) value).longValue();
					} else if (field.getType() == String.class && value != null) {
						convertedValue = String.valueOf(value);
					}
					ReflectionUtils.setField(field, existingUser, convertedValue);
				}
			}
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
