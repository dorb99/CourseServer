package com.courseServer.enteties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserDto {
	@NotBlank(message = "Name cannot be blank")
	private String name;
	
	@NotNull(message = "Age cannot be null")
	@Min(value = 0, message = "Age must be positive")
	private Integer age;
	
	public UserDto() {}
	
	public UserDto(String name, int age) {
		this.name = name;
		this.age = age;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
}
