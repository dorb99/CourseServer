package com.courseServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
public class CourseServerApplication {

	public static void main(String[] args) {
		System.out.println("Starting our server");
		SpringApplication.run(CourseServerApplication.class, args);
	}
}
