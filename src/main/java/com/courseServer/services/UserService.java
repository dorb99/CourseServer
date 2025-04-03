package com.courseServer.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import com.courseServer.enteties.User;
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
	public Optional<User> getOne(Long id) {
		return repository.findById(id);
	}
	
	// Create
	public boolean create(User user) {
		// get the data from the body
		if(repository.save(user) != null) {
			return true;			
		}
		return false;	
	}

	// Update
	public boolean update(User user) {
		// get the data from the body
		if(repository.save(user) != null) {
			return true;			
		}
		return false;	
	}

	
	// Delete
	public void delete(Long id) {
		repository.deleteById(id);
	}
}
