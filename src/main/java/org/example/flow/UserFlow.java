package org.example.flow;

import org.example.pojo.UserPojo;
import org.example.api.UserApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.exception.ApiException;

@Service
public class UserFlow {

    @Autowired
    private UserApi api;

    protected String getEntityName() {
        return "User";
    }

    public void signup(UserPojo userPojo) {
        api.signup(userPojo);
    }

    public UserPojo getByEmail(String email) {
        return api.getByEmail(email);
    }

    public boolean checkPassword(String rawPassword, String hashedPassword) {
        return api.checkPassword(rawPassword, hashedPassword);
    }

    public UserPojo get(Integer id) {
        if (id == null) {
            throw new ApiException("User ID cannot be null");
        }
        return api.get(id);
    }

    public void add(UserPojo userPojo) {
        api.add(userPojo);
    }

    public void update(Integer id, UserPojo userPojo) {
        if (id == null) {
            throw new ApiException("User ID cannot be null");
        }
        if (userPojo == null) {
            throw new ApiException("User cannot be null");
        }
        api.update(id, userPojo);
    }

    public java.util.List<UserPojo> getAll() {
        return api.getAll();
    }
}

