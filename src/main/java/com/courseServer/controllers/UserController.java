package com.courseServer.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.courseServer.enteties.User;
import com.courseServer.services.UserService;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
	
	private final UserService service;
	
	public UserController(UserService service) {
		this.service = service;
	}
	
	
	
	// Get all
	@GetMapping()
	public List<User> getAll() {
		return service.getAll();
	}

	// Get one
	@GetMapping("/{id}")
	public Optional<User> getOne(@PathVariable(name = "id") Long id) {
		return service.getOne(id);
	}
	
	// Create
	@PostMapping("/{name}/{age}")
	public boolean create(@PathVariable(name = "name") String name, @PathVariable(name = "age") int age) {
			return service.create(new User(name, age));
	}

//	// Update
//	@PutMapping("/{name}")
//	public String update(@PathVariable(name = "id") Long id) {
//		return "Hello " + ", from the server";
//	}

	
	// Delete
	@DeleteMapping("/{id}")
	public void delete(@PathVariable(name = "id") Long id) {
		service.delete(id);
	}
}