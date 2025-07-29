package org.example.flow;

import org.example.api.AbstractApi;
import org.example.exception.ApiException;
import org.example.model.form.PaginationRequest;
import org.example.model.form.PaginationQuery;
import org.example.model.data.PaginationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.lang.reflect.Method;

@Service
public abstract class AbstractFlow<T> {

    @Autowired
    protected AbstractApi<T> api;

    protected final Class<T> entityClass;

    protected AbstractFlow(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Add a new entity
     */
    public T add(T entity) {
        api.add(entity);
        return entity;
    }

    /**
     * Update entity by ID
     */
    @Transactional
    public void update(Integer id, T entity) {
        if (id == null) {
            throw new ApiException("ID cannot be null");
        }
        if (entity == null) {
            throw new ApiException("Entity cannot be null");
        }
        api.update(id, entity);
    }

    /**
     * Get all entities
     */
    public List<T> getAll() {
        return api.getAll();
    }

    /**
     * Get entity by field value
     */
    public T getByField(String fieldName, Object value) {
        if (fieldName == null || value == null) {
            throw new ApiException("Field name and value cannot be null");
        }
        return api.getByField(fieldName, value);
    }

    /**
     * Find entity by field value (returns null if not found)
     */
    public T findByField(String fieldName, Object value) {
        return api.findByField(fieldName, value);
    }

    /**
     * Get entities by multiple field values
     */
    public List<T> getByFields(String[] fieldNames, Object[] values) {
        if (fieldNames == null || values == null) {
            throw new ApiException("Field names and values cannot be null");
        }
        return api.getByFields(fieldNames, values);
    }

    /**
     * Validate entity exists by ID
     */
    protected void validateEntityExists(Integer id) {
        if (id == null) {
            throw new ApiException("ID cannot be null");
        }
        T entity = api.get(id);
        if (entity == null) {
            throw new ApiException(getEntityName() + " with ID " + id + " not found");
        }
    }

    /**
     * Validate entity exists by field value
     */
    protected void validateEntityExistsByField(String fieldName, Object value) {
        if (fieldName == null || value == null) {
            throw new ApiException("Field name and value cannot be null");
        }
        T entity = api.getByField(fieldName, value);
        if (entity == null) {
            throw new ApiException(getEntityName() + " with " + fieldName + " = " + value + " not found");
        }
    }

    /**
     * Get entity ID from entity object
     */
    protected Integer getEntityId(T entity) {
        if (entity == null) {
            throw new ApiException("Entity cannot be null");
        }
        try {
            Method getIdMethod = entity.getClass().getMethod("getId");
            return (Integer) getIdMethod.invoke(entity);
        } catch (Exception e) {
            throw new ApiException("Unable to get entity ID: " + e.getMessage());
        }
    }

    /**
     * Get entity name for error messages
     */
    protected String getEntityName() {
        return entityClass.getSimpleName();
    }

    // ========== GENERALIZED PAGINATION METHOD ==========

    /**
     * Generalized pagination method that handles all types of queries.
     * This method replaces the need for multiple specific pagination methods.
     */
    public PaginationResponse<T> getPaginated(PaginationQuery query) {
        if (query == null) {
            throw new ApiException("Pagination query cannot be null");
        }
        return api.getPaginated(query);
    }

} 