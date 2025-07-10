package org.example.dto;

import org.example.flow.UserFlow;
import org.example.model.UserData;
import org.example.model.UserForm;
import org.example.pojo.UserPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;

@Component
public class UserDto extends AbstractDto<UserPojo, UserForm, UserData> {

    @Autowired
    private UserFlow userFlow;

    @Override
    protected String getEntityName() {
        return "User";
    }

    @Override
    protected void preprocess(UserForm form) {
        // Cross-field/entity logic: email normalization
        if (form.getEmail() != null) {
            form.setEmail(form.getEmail().trim().toLowerCase());
        }
        if (form.getPassword() != null) {
            form.setPassword(form.getPassword().trim());
        }
    }

    @Override
    protected UserPojo convertFormToEntity(UserForm form) {
        UserPojo pojo = new UserPojo();
        pojo.setEmail(form.getEmail().toLowerCase().trim());
        pojo.setPassword(form.getPassword().trim());
        // Set role from form
        if (form.getRole() != null && form.getRole().equalsIgnoreCase("SUPERVISOR")) {
            pojo.setRole(org.example.enums.Role.SUPERVISOR);
        } else {
            pojo.setRole(org.example.enums.Role.USER);
        }
        return pojo;
    }

    @Override
    protected UserData convertEntityToData(UserPojo pojo) {
        UserData data = new UserData();
        data.setId(pojo.getId());
        data.setEmail(pojo.getEmail());
        data.setRole(pojo.getRole());
        return data;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public UserData update(Integer id, @Valid UserForm form) {
        return super.update(id, form);
    }

    @Transactional
    public void signup(@Valid UserForm form) {
        preprocess(form);
        UserPojo pojo = convertFormToEntity(form);
        userFlow.signup(pojo);
    }

    public UserData login(@Valid UserForm form) {
        preprocess(form);
        UserPojo pojo = userFlow.getByEmail(form.getEmail());
        if (pojo == null || !userFlow.checkPassword(form.getPassword(), pojo.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return convertEntityToData(pojo);
    }

    public UserData getUserByEmail(String email) {
        UserPojo pojo = userFlow.getByEmail(email);
        if (pojo == null) {
            throw new RuntimeException("User not found");
        }
        return convertEntityToData(pojo);
    }
}
