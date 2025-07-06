package org.example.flow;

import org.example.pojo.UserPojo;
import org.example.api.UserApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserFlow {

    @Autowired
    private UserApi api;

    public void signup(UserPojo userPojo) {
        api.signup(userPojo);
    }

    public UserPojo getByEmail(String email) {
        return api.getByEmail(email);
    }

    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return api.checkPassword(rawPassword, hashedPassword);
    }
}

