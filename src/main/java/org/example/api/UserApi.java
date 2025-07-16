package org.example.api;

import org.example.config.SecurityConfig;
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
import java.util.Collections;

@Service
public class UserApi extends AbstractApi<UserPojo> implements UserDetailsService {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private UserDao userDao;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    protected String getEntityName() {
        return "User";
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