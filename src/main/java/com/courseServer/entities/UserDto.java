package com.courseServer.entities;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String name;
    private Integer age;

    public UserDto() {}

    public UserDto(Long id, String name, Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public static UserDto fromUser(User user) {
        return new UserDto(user.getId(), user.getName(), user.getAge());
    }
} 