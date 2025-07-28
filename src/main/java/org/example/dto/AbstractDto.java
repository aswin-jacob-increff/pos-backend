package org.example.dto;

import org.example.exception.ApiException;
import org.example.api.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.validation.Valid;

@Component
public abstract class AbstractDto<T, F, D> {

    @Autowired
    protected AbstractApi<T> api;

    public D add(@Valid F form) {
        preprocess(form);
        T entity = convertFormToEntity(form);
        api.add(entity);
        return convertEntityToData(entity);
    }

    public D get(Integer id) {
        validateId(id);
        T entity = api.get(id);
        return convertEntityToData(entity);
    }

    public List<D> getAll() {
        List<T> entities = api.getAll();
        List<D> dataList = new ArrayList<>();
        for (T entity : entities) {
            dataList.add(convertEntityToData(entity));
        }
        return dataList;
    }

    @org.springframework.transaction.annotation.Transactional
    public D update(Integer id, @Valid F form) {
        validateId(id);
        preprocess(form);
        T entity = convertFormToEntity(form);
        api.update(id, entity);
        return convertEntityToData(api.get(id));
    }

    /**
     * Get entity name for error messages
     */
    protected abstract String getEntityName();

    /**
     * Convert form to entity
     */
    protected abstract T convertFormToEntity(F form);

    /**
     * Convert entity to data
     */
    protected abstract D convertEntityToData(T entity);

    /**
     * Preprocess form before conversion
     */
    protected void preprocess(F form) {
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
     * Generic method to get by field
     */
    protected D getByField(String fieldName, Object value) {
        T entity = api.getByField(fieldName, value);
        return convertEntityToData(entity);
    }

    /**
     * Generic method to find by field (returns null if not found)
     */
    protected D findByField(String fieldName, Object value) {
        T entity = api.findByField(fieldName, value);
        return entity != null ? convertEntityToData(entity) : null;
    }

    /**
     * Generic method to get by multiple fields
     */
    protected List<D> getByFields(String[] fieldNames, Object[] values) {
        List<T> entities = api.getByFields(fieldNames, values);
        List<D> dataList = new ArrayList<>();
        for (T entity : entities) {
            dataList.add(convertEntityToData(entity));
        }
        return dataList;
    }
} 