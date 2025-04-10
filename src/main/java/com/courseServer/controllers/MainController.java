package com.courseServer.controllers;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.courseServer.enteties.LoginDto;
import com.courseServer.utils.JwtUtil;

@RestController
@RequestMapping("/api/v1/main")
public class MainController {
	
	@GetMapping()
	public String sayHi() {
		return "Hello, from the server";
	}
	
	@GetMapping("/sec")
	public String sayHiSecured(@CookieValue(value = "token", required = true) String cookie) {
		String name = JwtUtil.validateToken(cookie);
		if(name == null) {
			return "false"; 
		}
		return "Hello "+name+" from a secure location";
	}
	
	@GetMapping("/name")
	public String sayHiName(@RequestParam(name="name", required=false, defaultValue="sasi") String name) {
		return "Hello "+name+", from the server";
	}
	
	@GetMapping("/{name}")
	public String sayHi(@PathVariable String name) {
		return "Hello "+name+", from the server";
	}
	
}