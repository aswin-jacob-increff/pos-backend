package org.example.api;

import org.example.dao.AbstractDao;
import org.example.exception.ApiException;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.example.model.data.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public abstract class AbstractApi<T> {

    @Autowired
    protected AbstractDao<T> dao;

    protected final Class<T> entityClass;

    protected AbstractApi(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Add a new entity
     */
    public void add(T entity) {
        if (entity == null) {
            throw new ApiException(getEntityName() + " cannot be null");
        }
        dao.insert(entity);
    }

    /**
     * Get entity by ID
     */
    public T get(Integer id) {
        validateId(id);
        T entity = dao.select(id);
        if (entity == null) {
            throw new ApiException(getEntityName() + " with ID " + id + " not found");
        }
        return entity;
    }

    /**
     * Update entity by ID
     */
    @org.springframework.transaction.annotation.Transactional
    public void update(Integer id, T entity) {
        validateId(id);
        if (entity == null) {
            throw new ApiException(getEntityName() + " cannot be null");
        }
        dao.update(id, entity);
    }

    /**
     * Delete entity by ID
     */
    @org.springframework.transaction.annotation.Transactional
    public void delete(Integer id) {
        validateId(id);
        // Note: Implement actual deletion logic in subclasses if needed
        throw new ApiException("Delete operation not implemented for " + getEntityName());
    }

    /**
     * Get all entities
     */
    public List<T> getAll() {
        return dao.selectAll();
    }

    /**
     * Get entity name for error messages
     */
    protected String getEntityName() {
        return entityClass.getSimpleName();
    }

    /**
     * Validate entity ID
     */
    protected void validateId(Integer id) {
        if (Objects.isNull(id)) {
            throw new ApiException(getEntityName() + " ID cannot be null");
        }
        if (id <= 0) {
            throw new ApiException(getEntityName() + " ID must be positive");
        }
    }

    // ========== ENHANCED VALIDATION METHODS ==========

    /**
     * Validate that a string is not null or empty
     */
    protected void validateString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ApiException(fieldName + " cannot be null or empty");
        }
    }

    /**
     * Validate that a number is positive
     */
    protected void validatePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new ApiException(fieldName + " must be positive");
        }
    }

    /**
     * Validate that a number is non-negative
     */
    protected void validateNonNegative(Integer value, String fieldName) {
        if (value == null || value < 0) {
            throw new ApiException(fieldName + " cannot be negative");
        }
    }

    /**
     * Validate date range (start date must be before or equal to end date)
     */
    protected void validateDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ApiException("Start date and end date cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new ApiException("End date cannot be before start date");
        }
    }

    // ========== ENHANCED SEARCH METHODS ==========

    /**
     * Generic method to find by a single field value
     */
    public T getByField(String fieldName, Object value) {
        T entity = dao.selectByField(fieldName, value);
        if (Objects.isNull(entity)) {
            throw new ApiException(getEntityName() + " with " + fieldName + " '" + value + "' not found");
        }
        return entity;
    }

    /**
     * Generic method to find by a single field value (returns null if not found)
     */
    public T findByField(String fieldName, Object value) {
        return dao.selectByField(fieldName, value);
    }

    /**
     * Generic method to find by multiple field values
     */
    public List<T> getByFields(String[] fieldNames, Object[] values) {
        return dao.selectByFields(fieldNames, values);
    }

    /**
     * Get entity by name field with validation
     */
    public T getByName(String name) {
        validateString(name, "Name");
        return getByField("name", name.trim());
    }

    /**
     * Get entity by name field with validation (returns null if not found)
     */
    public T findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return findByField("name", name.trim());
    }

    /**
     * Get entities by name pattern matching
     */
    public List<T> getByNameLike(String name) {
        validateString(name, "Name");
        return dao.selectByFieldLike("name", name.trim());
    }

    /**
     * Get entity by any field with validation
     */
    public T getByFieldWithValidation(String fieldName, Object value, String fieldDisplayName) {
        validateString(fieldName, "Field name");
        if (value == null) {
            throw new ApiException(fieldDisplayName + " cannot be null");
        }
        return getByField(fieldName, value);
    }

    /**
     * Get entities by field pattern matching with validation
     */
    public List<T> getByFieldLikeWithValidation(String fieldName, String value, String fieldDisplayName) {
        validateString(fieldName, "Field name");
        validateString(value, fieldDisplayName);
        return dao.selectByFieldLike(fieldName, value.trim());
    }

    // ========== ENHANCED PAGINATION METHODS ==========

    /**
     * Generalized pagination method that handles all types of queries.
     * This method replaces the need for multiple specific pagination methods.
     */
    public PaginationResponse<T> getPaginated(PaginationQuery query) {
        if (query == null) {
            throw new ApiException("Pagination query cannot be null");
        }
        return dao.getPaginated(query);
    }

    /**
     * Get paginated results by field with validation
     */
    public PaginationResponse<T> getByFieldPaginatedWithValidation(String fieldName, Object value, PaginationRequest request, String fieldDisplayName) {
        validateString(fieldName, "Field name");
        if (value == null) {
            throw new ApiException(fieldDisplayName + " cannot be null");
        }
        if (request == null) {
            request = new PaginationRequest();
        }
        return dao.selectByFieldPaginated(fieldName, value, request);
    }

    /**
     * Get paginated results by field pattern matching with validation
     */
    public PaginationResponse<T> getByFieldLikePaginatedWithValidation(String fieldName, String value, PaginationRequest request, String fieldDisplayName) {
        validateString(fieldName, "Field name");
        validateString(value, fieldDisplayName);
        if (request == null) {
            request = new PaginationRequest();
        }
        return dao.selectByFieldLikePaginated(fieldName, value.trim(), request);
    }

    // ========== LEGACY PAGINATION METHODS (for backward compatibility) ==========

    /**
     * Get all entities with pagination support.
     * @deprecated Use getPaginated(PaginationQuery.all(request)) instead
     */
    @Deprecated
    public PaginationResponse<T> getAllPaginated(PaginationRequest request) {
        if (request == null) {
            request = new PaginationRequest();
        }
        return getPaginated(PaginationQuery.all(request));
    }

    /**
     * Get entities by field value with pagination support.
     * @deprecated Use getPaginated(PaginationQuery.byField(fieldName, value, request)) instead
     */
    @Deprecated
    public PaginationResponse<T> getByFieldPaginated(String fieldName, Object value, PaginationRequest request) {
        if (request == null) {
            request = new PaginationRequest();
        }
        return getPaginated(PaginationQuery.byField(fieldName, value, request));
    }

    /**
     * Get entities by field value with partial string matching and pagination support.
     * @deprecated Use getPaginated(PaginationQuery.byFieldLike(fieldName, searchPattern, request)) instead
     */
    @Deprecated
    public PaginationResponse<T> getByFieldLikePaginated(String fieldName, String searchPattern, PaginationRequest request) {
        if (request == null) {
            request = new PaginationRequest();
        }
        return getPaginated(PaginationQuery.byFieldLike(fieldName, searchPattern, request));
    }
} 