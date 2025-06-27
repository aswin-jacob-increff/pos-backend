package org.example.dto;

import org.example.flow.UserFlow;
import org.example.model.UserData;
import org.example.model.UserForm;
import org.example.pojo.UserPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDto {

    @Autowired
    private UserFlow userFlow;

    public void signup(UserForm form) {
        validate(form);
        UserPojo pojo = new UserPojo();
        pojo.setEmail(form.getEmail().toLowerCase().trim());
        pojo.setPassword(form.getPassword().trim());
        userFlow.signup(pojo);
    }

    public UserData login(UserForm form) {
        validate(form);
        UserPojo pojo = userFlow.getByEmail(form.getEmail());
        if (pojo == null || !userFlow.checkPassword(form.getPassword(), pojo.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return convert(pojo);
    }

    private void validate(UserForm form) {
        if (form.getEmail() == null || form.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email cannot be empty");
        }
        if (form.getPassword() == null || form.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }
    }

    private UserData convert(UserPojo pojo) {
        UserData data = new UserData();
        data.setId(pojo.getId());
        data.setEmail(pojo.getEmail());
        data.setRole(pojo.getRole());
        return data;
    }
}
