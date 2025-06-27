package org.example.flow;

import org.example.pojo.UserPojo;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserFlow {

    @Autowired
    private UserService userService;

    public void signup(UserPojo userPojo) {
        userService.signup(userPojo);
    }

    public UserPojo getByEmail(String email) {
        return userService.getByEmail(email);
    }

    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return userService.checkPassword(rawPassword, hashedPassword);
    }
}

