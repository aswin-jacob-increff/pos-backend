package org.example.dto;

import org.example.exception.ApiException;
import org.example.pojo.UserPojo;
import org.example.model.data.UserData;
import org.example.model.form.UserForm;
import org.example.model.data.PaginationResponse;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.example.flow.UserFlow;
import org.example.api.UserApi;
import org.example.model.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Objects;

@Component
public class UserDto {

    @Autowired
    private UserFlow userFlow;

    @Autowired
    private UserApi userApi;

    @Value("${supervisor.email:admin@example.com}")
    private String supervisorEmail;

    protected String getEntityName() {
        return "User";
    }

    protected void preprocess(UserForm form) {
        // Cross-field/entity logic: email normalization
        if (form.getEmail() != null) {
            form.setEmail(form.getEmail().trim().toLowerCase());
        }
        if (form.getPassword() != null) {
            form.setPassword(form.getPassword().trim());
        }
    }

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

    protected UserData convertEntityToData(UserPojo pojo) {
        UserData data = new UserData();
        data.setId(pojo.getId());
        data.setEmail(pojo.getEmail());
        data.setRole(pojo.getRole());
        return data;
    }

    public UserData add(@Valid UserForm form) {
        preprocess(form);
        UserPojo entity = convertFormToEntity(form);
        userApi.add(entity);
        return convertEntityToData(entity);
    }

    public UserData get(Integer id) {
        validateId(id);
        UserPojo entity = userApi.get(id);
        return convertEntityToData(entity);
    }

    public List<UserData> getAll() {
        List<UserPojo> entities = userApi.getAll();
        List<UserData> dataList = new java.util.ArrayList<>();
        for (UserPojo entity : entities) {
            dataList.add(convertEntityToData(entity));
        }
        return dataList;
    }

    @Transactional
    public UserData update(Integer id, @Valid UserForm form) {
        validateId(id);
        preprocess(form);
        UserPojo entity = convertFormToEntity(form);
        userApi.update(id, entity);
        return convertEntityToData(userApi.get(id));
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
    public PaginationResponse<UserData> getAllPaginated(PaginationRequest request) {
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
        
        return new PaginationResponse<>(
            paginatedContent,
            totalElements,
            pageNumber,
            pageSize
        );
    }

    /**
     * Get paginated results using PaginationQuery
     */
    public PaginationResponse<UserData> getPaginated(PaginationQuery query) {
        if (query == null) {
            throw new ApiException("Pagination query cannot be null");
        }
        
        // For now, we only support ALL query type for users
        if (query.getQueryType() != PaginationQuery.QueryType.ALL) {
            throw new ApiException("Only ALL query type is supported for users");
        }
        
        return getAllPaginated(query.getPaginationRequest());
    }

    /**
     * Validate ID parameter
     */
    protected void validateId(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException(getEntityName() + " ID cannot be null");
        }
        if (id <= 0) {
            throw new ApiException(getEntityName() + " ID must be positive");
        }
    }
}
