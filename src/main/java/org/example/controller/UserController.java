package org.example.controller;

import org.example.dto.UserDto;
import org.example.model.UserData;
import org.example.model.UserForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserDto userDto;

    // Signup API
    @PostMapping("/signup")
    public void signup(@RequestBody UserForm form) {
        userDto.signup(form);
    }

    // Login API
    @PostMapping("/login")
    public UserData login(@RequestBody UserForm form) {
        return userDto.login(form);
    }
}
