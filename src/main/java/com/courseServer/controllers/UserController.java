package com.courseServer.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.courseServer.enteties.User;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
	// Get all
	@GetMapping()
	public List<User> getAll() {
		return null;
	}

	// Get one
	@GetMapping("/{id}")
	public Optional<User> getOne(@PathVariable(name = "id") Long id) {
		return null;
	}
	
	// Create
	@PostMapping()
	public String create() {
		// get the data from the body
		return "Hello #, from the server";
	}

	// Update
	@PutMapping("/{name}")
	public String update(@PathVariable(name = "id") Long id) {
		return "Hello " + ", from the server";
	}

	
	// Delete
	@DeleteMapping("/{name}")
	public String delete(@PathVariable(name = "id") Long id) {
		return "Hello " + ", from the server";
	}
}