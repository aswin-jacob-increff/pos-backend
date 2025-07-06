package org.example.api;

import org.example.config.SecurityConfig;
import org.example.dao.UserDao;
import org.example.enums.Role;
import org.example.pojo.UserPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserApi {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private UserDao userDao;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Signup method that accepts a pre-constructed UserPojo (used by UserFlow)
    @Transactional
    public void signup(UserPojo userPojo) {
        // Assign role based on email
        Role role = securityConfig.isSupervisor(userPojo.getEmail()) ? Role.SUPERVISOR : Role.USER;

        // Normalize and hash password
        userPojo.setEmail(userPojo.getEmail().toLowerCase().trim());
        userPojo.setPassword(passwordEncoder.encode(userPojo.getPassword()));
        userPojo.setRole(role);

        // Save user
        userDao.insert(userPojo);
    }

    // Fetch user by email (used in login validation)
    public UserPojo getByEmail(String email) {
        return userDao.getByEmail(email);
    }

    // Check if raw password matches the encoded password
    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
} 