package org.example.dto;

import org.example.flow.UserFlow;
import org.example.model.UserData;
import org.example.model.UserForm;
import org.example.pojo.UserPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;

@Component
public class UserDto extends AbstractDto<UserPojo, UserForm, UserData> {

    @Autowired
    private UserFlow userFlow;

    @Value("${supervisor.email:admin@example.com}")
    private String supervisorEmail;

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
        
        // Automatically set role to USER for all signups
        // Supervisor accounts are only created via application.properties
        pojo.setRole(org.example.enums.Role.USER);
        
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
        
        // Prevent creation of supervisor accounts via signup
        if (form.getEmail().equalsIgnoreCase(supervisorEmail)) {
            throw new RuntimeException("Cannot create supervisor account via signup. Supervisor accounts are pre-configured.");
        }
        
        UserPojo pojo = convertFormToEntity(form);
        userFlow.signup(pojo);
    }

    public UserData getUserByEmail(String email) {
        UserPojo pojo = userFlow.getByEmail(email);
        if (pojo == null) {
            throw new RuntimeException("User not found");
        }
        return convertEntityToData(pojo);
    }
}
