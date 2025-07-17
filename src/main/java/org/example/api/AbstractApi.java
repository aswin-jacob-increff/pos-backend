package org.example.api;

import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.dao.AbstractDao;
import java.util.List;
import java.util.Objects;

@Service
public abstract class AbstractApi<T> {

    @Autowired
    protected AbstractDao<T> dao;

    public void add(T entity) {
        validateAdd(entity);
        dao.insert(entity);
    }

    public T get(Integer id) {
        validateId(id);
        T entity = dao.select(id);
        if (Objects.isNull(entity)) {
            throw new ApiException(getEntityName() + " with ID " + id + " not found");
        }
        return entity;
    }

    public List<T> getAll() {
        List<T> entities = dao.selectAll();
        if (Objects.isNull(entities) || entities.isEmpty()) {
            throw new ApiException("No " + getEntityName().toLowerCase() + "s found");
        }
        return entities;
    }

    public void update(Integer id, T updatedEntity) {
        validateId(id);
        T existing = dao.select(id);
        if (Objects.isNull(existing)) {
            throw new ApiException(getEntityName() + " with ID " + id + " not found");
        }
        validateUpdate(existing, updatedEntity);
        dao.update(id, updatedEntity);
    }

    /**
     * Get entity name for error messages
     */
    protected abstract String getEntityName();

    /**
     * Validate entity before adding
     */
    protected void validateAdd(T entity) {
        // Default implementation - subclasses can override
    }

    /**
     * Validate entity before updating
     */
    protected void validateUpdate(T existing, T updated) {
        // Default implementation - subclasses can override
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
} 