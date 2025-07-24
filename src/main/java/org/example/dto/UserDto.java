package org.example.dto;

import org.example.flow.UserFlow;
import org.example.model.data.UserData;
import org.example.model.enums.Role;
import org.example.model.form.UserForm;
import org.example.pojo.UserPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;

import java.util.List;

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
        
        // Check if email is in supervisors list
        String normalizedEmail = form.getEmail().toLowerCase().trim();
        System.out.println("UserDto: Checking role for email: " + normalizedEmail);
        System.out.println("UserDto: Supervisor email from config: " + supervisorEmail);
        System.out.println("UserDto: Supervisor email from constants: " + org.example.model.constants.Supervisors.ADMIN);
        
        if (normalizedEmail.equals(supervisorEmail.toLowerCase().trim()) || 
            normalizedEmail.equals(org.example.model.constants.Supervisors.ADMIN.toLowerCase().trim())) {
            pojo.setRole(Role.SUPERVISOR);
            System.out.println("UserDto: Setting role to SUPERVISOR for email: " + normalizedEmail);
        } else {
            pojo.setRole(Role.USER);
            System.out.println("UserDto: Setting role to USER for email: " + normalizedEmail);
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

    public UserData getUserByEmail(String email) {
        UserPojo pojo = userFlow.getByEmail(email);
        if (pojo == null) {
            throw new RuntimeException("User not found");
        }
        return convertEntityToData(pojo);
    }

    // ========== PAGINATION METHODS ==========

    /**
     * Get all users with pagination support.
     */
    public org.example.model.data.PaginationResponse<UserData> getAllPaginated(org.example.model.form.PaginationRequest request) {
        // Since UserFlow doesn't have pagination methods yet, we'll implement manual pagination
        List<UserData> allUsers = getAll();
        
        // Apply pagination manually
        int totalElements = allUsers.size();
        int pageSize = request.getPageSize();
        int pageNumber = request.getPageNumber();
        int startIndex = pageNumber * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalElements);
        
        List<UserData> paginatedContent;
        if (startIndex >= totalElements) {
            paginatedContent = List.of();
        } else {
            paginatedContent = allUsers.subList(startIndex, endIndex);
        }
        
        return new org.example.model.data.PaginationResponse<>(
            paginatedContent,
            totalElements,
            pageNumber,
            pageSize
        );
    }
}
