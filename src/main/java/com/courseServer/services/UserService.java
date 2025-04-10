package com.courseServer.services;

import java.net.HttpCookie;
import java.util.List;
import java.util.Optional;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import com.courseServer.entities.LoginDto;
import com.courseServer.entities.User;
import com.courseServer.entities.UserDto;
import com.courseServer.exceptions.UserNotFoundException;
import com.courseServer.repository.UserRepo;
import com.courseServer.utils.JwtUtil;
import com.courseServer.utils.Security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserService {

	private final UserRepo repository;
	
	@Autowired
	public UserService(UserRepo repository) {
		this.repository = repository; // Assign the injected repository
	}
	
	// Get all
	public List<User> getAll() {
		Iterable<User> users = repository.findAll();
		System.out.println(users);
		return (List<User>) users;
	}

	// Get one
	public User getOne(Long id) {
		return repository.findById(id)
				.orElseThrow(()-> new UserNotFoundException("Incorrect id: "+id));
	}
	
	// Get one by name
	public User getOneByName(String name) {
		User user;
		if((user = repository.findByName(name)) == null) {
			new UserNotFoundException("Incorrect user name: "+name);
		} 
		return user;
	}
	
	// Create
	public User create(UserDto userDto) {
		System.out.println("Starting at create service \t"+userDto.getPassword());

		// In bigger applications we will prefer to use a Mapper class that transform all classes to and from DTO.
		User user = new User(userDto.getName(), userDto.getPassword(), userDto.getAge());
		System.out.println("After creationg the user in service \t"+user.getPassword());
		return repository.save(user);
	}

	// Update
	public User update(Long id, UserDto userDto) {
		User existingUser = getOne(id);

		existingUser.setAge(userDto.getAge());
		existingUser.setName(userDto.getName());
		
		return repository.save(existingUser);
	}

	// Delete
	public void delete(Long id) {
		repository.deleteById(id);
	}
	
	public ResponseEntity<String> login(LoginDto loginUser) {
		// In real application we will return a user - the user who logged in. to save his information, to use the account
		User user = this.getOneByName(loginUser.getName());
		if(Security.checkPassword(loginUser.getPassword(), user.getPassword())) {
			String jwtToken = JwtUtil.createToken(user.getName());
			
			// when using the default springboot login action
//			public ResponseEntity<?> login(@RequestParam String username,
//                    @RequestParam String password,
//                    HttpServletResponse response) 
//			HttpCookie jwtCookie = new HttpCookie("Ticket", token);
//			jwtCookie.setHttpOnly(true);
//			jwtCookie.setPath("/catalog");
//			jwtCookie.setMaxAge(3600);
//			response.addCookie(jwtCookie);
			
			HttpHeaders header = new HttpHeaders();
			header.add("Set-Cookie", String.format("token=%s; Path=/; Max-Age=3600; HttpOnly;", jwtToken));
			
			return ResponseEntity.ok()
					.headers(header)
					.body("You are now logged in with a token");
		} else {
			return ResponseEntity.status(401)
					.body("Incrrect password or name");
		}
	}
}
