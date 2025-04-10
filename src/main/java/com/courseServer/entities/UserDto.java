package com.courseServer.entities;


import com.courseServer.utils.Security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class UserDto {
	
	@NotBlank(message = "Name must be not blank")
	private String name;
	
	
//	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$\r\n", message = "Must be a strong password: that enforces the following rules:\r\n"
//			+ "		At least 8 characters\r\n"
//			+ "		Contains at least 1 uppercase letter\r\n"
//			+ "		Contains at least 1 lowercase letter\r\n"
//			+ "		Contains at least 1 digit\r\n"
//			+ "		Contains at least 1 special character (e.g., @#$%^&+=!)")
	@NotBlank(message = "Password must be not blank")
	private String password;
	
	@NotNull(message = "Age must be not null")
	@Min(value = 0, message = "Age must be more then 0")
	private int age;
	
	public UserDto () {};
	
	public UserDto (String name, String password, int age) {
		this.name = name;
		this.password = Security.hashPassword(password);
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = Security.hashPassword(password);
	}
	
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	};
}
