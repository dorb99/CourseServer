package com.courseServer.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/main")
public class MainController {
	
	@GetMapping()
	public String sayHi() {
		return "Hello, from the server";
	}
	
	@GetMapping("/name")
	public String sayHiName(@RequestParam(name="name", required=false, defaultValue="sasi") String name) {
		return "Hello "+name+", from the server";
	}
	
	@GetMapping("/{name}")
	public String sayHi(@PathVariable String name) {
		return "Hello "+name+", from the server";
	}
	
	@PostMapping()
	public String sayBye() {
		// get the data from the body
		return "Hello #, from the server";
	}
}