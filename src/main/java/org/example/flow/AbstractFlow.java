package org.example.flow;

import org.example.api.AbstractApi;
import org.example.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public abstract class AbstractFlow<T> {

    @Autowired
    protected AbstractApi<T> api;

    /**
     * Add a new entity
     */
    public T add(T entity) {
        api.add(entity);
        return entity;
    }

    /**
     * Get entity by ID
     */
    public T get(Integer id) {
        return api.get(id);
    }

    /**
     * Get all entities
     */
    public List<T> getAll() {
        return api.getAll();
    }

    /**
     * Update entity by ID
     */
    @org.springframework.transaction.annotation.Transactional
    public T update(Integer id, T entity) {
        api.update(id, entity);
        return api.get(id);
    }

    /**
     * Delete entity by ID
     */
    public void delete(Integer id) {
        api.delete(id);
    }

    /**
     * Get entity by field value
     */
    public T getByField(String fieldName, Object value) {
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
        return api.getByFields(fieldNames, values);
    }

    /**
     * Delete entity by field value
     */
    public void deleteByField(String fieldName, Object value) {
        T entity = api.getByField(fieldName, value);
        if (entity == null) {
            throw new ApiException(getEntityName() + " with " + fieldName + " '" + value + "' not found");
        }
        api.delete(getEntityId(entity));
    }

    /**
     * Get entity ID - subclasses must implement this
     */
    protected abstract Integer getEntityId(T entity);

    /**
     * Get entity name for error messages
     */
    protected abstract String getEntityName();

    /**
     * Validate entity exists before operations
     */
    protected T validateEntityExists(Integer id) {
        T entity = api.get(id);
        if (entity == null) {
            throw new ApiException(getEntityName() + " with ID " + id + " not found");
        }
        return entity;
    }

    /**
     * Validate entity exists by field before operations
     */
    protected T validateEntityExistsByField(String fieldName, Object value) {
        T entity = api.getByField(fieldName, value);
        if (entity == null) {
            throw new ApiException(getEntityName() + " with " + fieldName + " '" + value + "' not found");
        }
        return entity;
    }

    /**
     * Safe delete with validation
     */
    public void safeDelete(Integer id) {
        validateEntityExists(id);
        api.delete(id);
    }

    /**
     * Safe delete by field with validation
     */
    public void safeDeleteByField(String fieldName, Object value) {
        validateEntityExistsByField(fieldName, value);
        deleteByField(fieldName, value);
    }
} 