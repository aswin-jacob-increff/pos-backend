package org.example.dto;

import org.example.flow.UserFlow;
import org.example.model.data.UserData;
import org.example.model.enums.Role;
import org.example.model.form.UserForm;
import org.example.pojo.UserPojo;
import org.example.model.constants.Supervisors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;

import java.util.List;

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
        // Normalize email and password
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
        pojo.setEmail(form.getEmail());
        pojo.setPassword(form.getPassword());
        
        // Simple role assignment: check if email matches supervisor email
        if (form.getEmail() != null && form.getEmail().equals(Supervisors.ADMIN.toLowerCase())) {
            pojo.setRole(Role.SUPERVISOR);
        } else {
            pojo.setRole(Role.USER);
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
    @Transactional
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
     * Note: This implementation loads all users into memory.
     * For better performance with large datasets, consider implementing
     * pagination at the DAO level.
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
