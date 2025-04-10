package com.courseServer.services;

import com.courseServer.entities.User;
import com.courseServer.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList; // For authorities list

/**
 * Service required by Spring Security to load user-specific data.
 * It retrieves user details (like username, password, authorities) based on the username.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepo userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Locates the user based on the username. In this implementation, the username
     * is the value stored in the 'name' field of our User entity.
     * @param username the username identifying the user whose data is required.
     * @return a fully populated UserDetails object (never null)
     * @throws UsernameNotFoundException if the user could not be found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        // Convert our User entity to Spring Security's UserDetails object.
        // The UserDetails object needs: username, password (hashed!), and authorities (roles/permissions).
        // For simplicity, we are not using roles here, so we provide an empty list of authorities.
        return new org.springframework.security.core.userdetails.User(
                user.getName(),
                user.getPassword(), // The hashed password from the database
                new ArrayList<>() // Empty list of GrantedAuthority (no specific roles defined yet)
        );
    }
} 