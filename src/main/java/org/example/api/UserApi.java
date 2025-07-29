package org.example.api;

import org.example.util.SecurityConfig;
import org.example.dao.UserDao;
import org.example.pojo.UserPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.example.exception.ApiException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class UserApi implements UserDetailsService {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private UserDao userDao;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Basic CRUD operations
    public void add(UserPojo userPojo) {
        if (Objects.isNull(userPojo)) {
            throw new ApiException("User cannot be null");
        }
        userDao.insert(userPojo);
    }

    public UserPojo get(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException("User ID cannot be null");
        }
        UserPojo user = userDao.select(id);
        if (user == null) {
            throw new ApiException("User with ID " + id + " not found");
        }
        return user;
    }

    public List<UserPojo> getAll() {
        return userDao.selectAll();
    }

    public void update(Integer id, UserPojo userPojo) {
        if (Objects.isNull(id)) {
            throw new ApiException("User ID cannot be null");
        }
        if (Objects.isNull(userPojo)) {
            throw new ApiException("User cannot be null");
        }
        UserPojo existingUser = userDao.select(id);
        if (existingUser == null) {
            throw new ApiException("User with ID " + id + " not found");
        }
        userDao.update(id, userPojo);
    }



    // Signup method that accepts a pre-constructed UserPojo (used by UserFlow)
    public void signup(UserPojo userPojo) {
        try {
            // Normalize and hash password
            userPojo.setEmail(userPojo.getEmail().toLowerCase().trim());
            userPojo.setPassword(passwordEncoder.encode(userPojo.getPassword()));
            // Role is already set on the pojo (from signup form)
            // Save user
            userDao.insert(userPojo);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Fetch user by email (used in login validation)
    public UserPojo getByEmail(String email) {
        return userDao.getByEmail(email);
    }

    // Check if raw password matches the encoded password
    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    // Spring Security UserDetailsService implementation
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserPojo user = getByEmail(email);
        
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return new User(
            user.getEmail(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
} 