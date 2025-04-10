package com.courseServer.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Column(unique = true, nullable = false)
    @Getter
    @Setter
    private String username;

    @Column(nullable = false)
    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private Integer age;

    public User(String username, String password, String name, Integer age) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "User - {\n\tId=" + id + ",\n\tUsername=" + username + ",\n\tName=" + name + ",\n\tAge=" + age + "\n}";
    }
} 