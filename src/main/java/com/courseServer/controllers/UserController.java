package com.courseServer.controllers;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.courseServer.enteties.LoginDto;
import com.courseServer.enteties.User;
import com.courseServer.enteties.UserDto;
import com.courseServer.exceptions.UserNotFoundException;
import com.courseServer.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
	
	private final UserService service;
	
	public UserController(UserService service) {
		this.service = service;
	}
	
	// Get all
	@GetMapping()
	public ResponseEntity<List<UserDto>> getAll() {
		List<User> users = service.getAll();
		List<UserDto> usersDto = users.stream()
						.map(this::convertToDto)
						.collect(Collectors.toList());
		
		return ResponseEntity.ok(usersDto);
	}

	// Get one
	@GetMapping("/{id}")
	public ResponseEntity<User> getOne(@PathVariable(name = "id") Long id) {
		User user = service.getOne(id);
		return ResponseEntity.ok(user);
	}
	
	// Create
	@PostMapping()
	public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userDto) {
		System.out.println("Creating in controller \t"+userDto.getPassword());
		User newUser = service.create(userDto);
		
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
							.path("/{id}")
							.buildAndExpand(newUser.getId())
							.toUri();
		return ResponseEntity.created(location).body(convertToDto(newUser));
	}

	// Update
	@PutMapping("/{id}")
	public User update(@PathVariable(name = "id") Long id, @Valid @RequestBody UserDto userDto) {
		User updatedUser = service.update(id, userDto);
		return updatedUser;
	}
	
	// Update
	@PatchMapping("/{id}")
	public ResponseEntity<UserDto> updatePart(@PathVariable(name = "id") Long id, @RequestBody UserDto userDto) {
		User updatedUser = service.update(id, userDto);
		return ResponseEntity.ok(convertToDto(updatedUser));
	}

	// Delete
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable(name = "id") Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
	
	private UserDto convertToDto(User user) {
		return new UserDto(user.getName(), user.getPassword(), user.getAge());
	}
	
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody LoginDto loginUser) {
		return service.login(loginUser);
	}
}